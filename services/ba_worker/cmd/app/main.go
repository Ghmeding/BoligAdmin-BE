package main

import (
	"ba_worker/internal/broker"
	"ba_worker/internal/services"
	"ba_worker/internal/util"
	"log"

	amqp "github.com/rabbitmq/amqp091-go"
)

func main() {
	brokerConnection, err := broker.CreateRabbitMQConnection("amqp://user:pass@localhost:5672/")

	err = brokerConnection.Channel.Qos(1, 0, false)
	util.CrashOnError(err, "Failed to connect to RabbitMQ")

	defer func(brokerConnection *amqp.Connection) {
		err := brokerConnection.Close()
		if err != nil {
			util.CrashOnError(err, "Failed to close connection to RabbitMQ")
		}
	}(brokerConnection.Conn)

	log.Println("Connected successfully!")

	msgs, err := brokerConnection.Channel.Consume(
		brokerConnection.Queue.Name,
		"",
		false,
		false,
		false,
		false,
		nil,
	)
	util.CrashOnError(err, "Failed to register worker")

	/* NOTE:
	 * Only reason to wrapping the loop in a go routine is to free up the main go-routine.
	 * Technically, it is not needed as we are doing anything else atm.
	 */
	go func() {
		for message := range msgs {
			log.Printf("Received: %s", message.Body)
			err := services.SendMail(string(message.Body))
			if err != nil {
				if util.IsTransient(err) {
					log.Printf("Temporary failure, requeueing...")

					// reject current message and requeue it
					message.Nack(false, true)
				} else {
					log.Printf("Permanent error %v. Moving message to DLQ.", err)
					message.Nack(false, false)
				}
				continue
			}

			// only acknowledge this message was successful
			message.Ack(false)
		}
	}()

	log.Printf("[*] Waiting for new messages from the queue")

	//block main from shutting the app down
	select {}
}
