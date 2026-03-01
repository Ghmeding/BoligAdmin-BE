package mailService

import "net/smtp"

func SendMail(body string) {
	from := "meding97@gmail.com"
	password := "vlna xgvy dvkl buma"

	toEmailAddress := "meding97@gmail.com"
	to := []string{toEmailAddress}

	host := "smtp.gmail.com"
	port := "587"
	address := host + ":" + port

	msg := []byte("To: " + toEmailAddress + "\r\n" +
		"Subject: TEST!\r\n" +
		"\r\n" +
		"This is the email body.\r\n")

	auth := smtp.PlainAuth("", from, password, host)

	err := smtp.SendMail(address, auth, from, to, msg)
	if err != nil {
		panic(err)
	}
}
