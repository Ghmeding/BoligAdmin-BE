package main

import (
	"ba_worker/mailService"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

func crashOnError(err error, msg string) {
	if err != nil {
		log.Panicf("%s, %s", err, msg)
	}
}

func main() {
	rabbitMQConnection, err := connectToRabbitMQWithRetry("amqp://user:pass@localhost:5672/", 5)
	crashOnError(err, "Failed to connect to RabbitMQ")

	defer func(rabbitMQConnection *amqp.Connection) {
		err := rabbitMQConnection.Close()
		if err != nil {
			crashOnError(err, "Failed to close connection to RabbitMQ")
		}
	}(rabbitMQConnection)

	log.Println("Connected successfully!")

	ch, err := rabbitMQConnection.Channel()
	crashOnError(err, "Failed to open consumer channel")

	// create the queue
	q, err := ch.QueueDeclare(
		"ba-worker-queue",
		true,
		false,
		false,
		false,
		nil,
	)
	crashOnError(err, "Failed to declare the queue")

	// bind the queue to the exchange
	err = ch.QueueBind(
		q.Name,
		"#", //routing key wildcard (accept everything from this exchange
		"property.tenant.created",
		false,
		nil,
	)
	crashOnError(err, "Failed to bind queue to exchange")

	msgs, err := ch.Consume(
		q.Name,
		"",
		true,
		false,
		false,
		false,
		nil,
	)
	crashOnError(err, "Failed to register consumer")

	/* NOTE:
	 * Only reason to wrapping the loop in a go routine is to free up the main go-routine.
	 * Technically, it is not needed as we are doing anything else atm.
	 */
	go func() {
		for message := range msgs {
			log.Printf("Received: %s", message.Body)
			mailService.SendMail(string(message.Body))
		}
	}()

	log.Printf("[*] Waiting for new messages from the queue")

	//block main from shutting the app down
	select {}
}

func connectToRabbitMQWithRetry(uri string, retries int) (*amqp.Connection, error) {
	var conn *amqp.Connection
	var err error

	for i := 1; i <= retries; i++ {
		log.Printf("Attempt %d...", i)
		conn, err = amqp.Dial(uri)

		if err == nil {
			return conn, nil
		}

		log.Printf("Failed to connect: %v", err)
		if i < 5 {
			time.Sleep(2 * time.Second)
		}
	}
	return nil, err
}
