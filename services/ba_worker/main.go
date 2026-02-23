package main

import (
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
	var rabbitMQConnection *amqp.Connection
	var err error
	//TODO: refactor retry block below
	for i := 1; i <= 5; i++ {
		log.Printf("Attempt %d...", i)
		rabbitMQConnection, err = amqp.Dial("amqp://user:pass@localhost:5672/")

		if err != nil {
			if i == 5 {
				crashOnError(err, "All 5 retries failed")
			}
			time.Sleep(2 * time.Second)
			continue
		}
		break // Success!
	}

	defer func(rabbitMQConnection *amqp.Connection) {
		err := rabbitMQConnection.Close()
		if err != nil {
			crashOnError(err, "Failed to close connection to RabbitMQ")
		}
	}(rabbitMQConnection)

	log.Println("Connected successfully!")

	ch, err := rabbitMQConnection.Channel()
	crashOnError(err, "Failed to open consumer channel")

	q, err := ch.QueueDeclare(
		"ba-worker-queue",
		true,
		false,
		false,
		false,
		nil,
	)
	crashOnError(err, "Failed to declare the queue")

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
		}
	}()

	log.Printf("[*] Waiting for new messages from the queue")

	//block main from shutting the app down
	select {}

}
