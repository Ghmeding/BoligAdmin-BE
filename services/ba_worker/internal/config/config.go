package config

import "os"

type Config struct {
	SMTPUser    string
	SMTPPass    string
	SMTPHost    string
	SMTPPort    string
	RabbitMQURL string
}

func New() *Config {
	return &Config{
		SMTPUser:    os.Getenv("SMTPUser"),
		SMTPPass:    os.Getenv("SMTPPass"),
		SMTPHost:    "smtp.gmail.com",
		SMTPPort:    "587",
		RabbitMQURL: os.Getenv("RabbitMQURL"),
	}
}
