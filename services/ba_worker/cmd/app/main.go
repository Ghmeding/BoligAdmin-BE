package main

import (
	"ba_worker/internal/broker"
	"ba_worker/internal/util"
	"ba_worker/internal/worker"
	"log"
	"sync"

	amqp "github.com/rabbitmq/amqp091-go"
)

func main() {
	brokerConnection, err := broker.CreateRabbitMQConnection("amqp://user:pass@localhost:5672/")

	err = brokerConnection.Channel.Qos(10, 0, false)
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

	workerCount := 5
	var wg sync.WaitGroup
	for i := 0; i < workerCount; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			worker.ProcessMessages(msgs, i)
		}()
	}

	log.Printf("[*] Waiting for new messages from the queue")
	wg.Wait()
}
