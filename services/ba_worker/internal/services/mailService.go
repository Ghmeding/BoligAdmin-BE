package services

import (
	"ba_worker/internal/config"
	"log"
	"net/smtp"
)

func SendMail(body string, cfg *config.Config) error {
	from := cfg.SMTPUser
	password := cfg.SMTPPass

	toEmailAddress := cfg.SMTPUser
	to := []string{toEmailAddress}

	host := cfg.SMTPHost
	port := "587"
	address := host + ":" + port

	msg := []byte("To: " + toEmailAddress + "\r\n" +
		"Subject: TEST!\r\n" +
		"\r\n" +
		"This is the email body.\r\n")

	auth := smtp.PlainAuth("", from, password, host)

	err := smtp.SendMail(address, auth, from, to, msg)
	if err != nil {
		return err
	}
	log.Printf("Mail sent successfully!")

	return nil
}
