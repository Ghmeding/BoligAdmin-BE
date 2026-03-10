package worker

import (
	"ba_worker/internal/config"
	"ba_worker/internal/services"
	"ba_worker/internal/util"
	"log"

	"github.com/rabbitmq/amqp091-go"
)

func ProcessMessages(msgs <-chan amqp091.Delivery, workerId int, cfg *config.Config) {
	log.Printf("worker%d processing msgs...", workerId)
	for message := range msgs {
		log.Printf("Received: %s", message.Body)
		err := services.SendMail(string(message.Body), cfg)

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
}
