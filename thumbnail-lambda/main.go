package main

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"image"
	"image/jpeg"
	_ "image/png"
	"net/url"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"golang.org/x/image/draw"
)

type S3EventRecord struct {
	S3 struct {
		Bucket struct {
			Name string `json:"name"`
		} `json:"bucket"`
		Object struct {
			Key string `json:"key"`
		} `json:"object"`
	} `json:"s3"`
}

type S3Event struct {
	Records []S3EventRecord `json:"Records"`
}

func handler(ctx context.Context, sqsEvent events.SQSEvent) error {

	cfg, err := config.LoadDefaultConfig(ctx, config.WithRegion("sa-east-1"))
	if err != nil {
		return fmt.Errorf("erro ao carregar config AWS: %w", err)
	}
	s3Client := s3.NewFromConfig(cfg)

	for _, sqsRecord := range sqsEvent.Records {
		var s3Event S3Event
		if err := json.Unmarshal([]byte(sqsRecord.Body), &s3Event); err != nil {
			return fmt.Errorf("S3: %w", err)
		}

		for _, s3Record := range s3Event.Records {
			bucket := s3Record.S3.Bucket.Name
			rawKey := s3Record.S3.Object.Key

			key, err := url.QueryUnescape(rawKey)
			if err != nil {
				return fmt.Errorf("erro ao decodar key: %w", err)
			}

			fmt.Printf("Processando: bucket=%s key=%s\n", bucket, key)

			result, err := s3Client.GetObject(ctx, &s3.GetObjectInput{
				Bucket: aws.String(bucket),
				Key:    aws.String(key),
			})
			if err != nil {
				return fmt.Errorf("erro download S3: %w", err)
			}
			defer result.Body.Close()

			img, _, err := image.Decode(result.Body)
			if err != nil {
				return fmt.Errorf("erro decodificar imagem: %w", err)
			}

			thumbnail := image.NewRGBA(image.Rect(0, 0, 200, 200))
			draw.BiLinear.Scale(thumbnail, thumbnail.Bounds(), img, img.Bounds(), draw.Over, nil)

			var buf bytes.Buffer
			if err := jpeg.Encode(&buf, thumbnail, &jpeg.Options{Quality: 85}); err != nil {
				return fmt.Errorf("erro gerar thumbnail: %w", err)
			}

			thumbnailKey := "thumbnails/" + key
			_, err = s3Client.PutObject(ctx, &s3.PutObjectInput{
				Bucket:      aws.String(bucket),
				Key:         aws.String(thumbnailKey),
				Body:        bytes.NewReader(buf.Bytes()),
				ContentType: aws.String("image/jpeg"),
			})
			if err != nil {
				return fmt.Errorf("erro salvar thumbnail no S3: %w", err)
			}

			fmt.Printf("Thumbnail salvo em: thumbnails/%s\n", key)
		}
	}
	return nil
}

func main() {
	lambda.Start(handler)
}
