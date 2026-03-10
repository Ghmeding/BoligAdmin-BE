package broker

import (
	"ba_worker/internal/config"
	"ba_worker/internal/util"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

type RabbitMQ struct {
	Conn    *amqp.Connection
	Channel *amqp.Channel
	Queue   amqp.Queue
}

func CreateRabbitMQConnection(cfg *config.Config) (*RabbitMQ, error) {
	rabbitMQConn, err := connectToRabbitMQWithRetry(cfg.RabbitMQURL, 5)
	util.CrashOnError(err, "Failed to establish connection to RabbitMQ")

	rabbitMQChannel, err := rabbitMQConn.Channel()
	util.CrashOnError(err, "Failed to create RabbitMQ channel")

	rabbitMQQueue, err := createQueue(rabbitMQChannel, "ba-worker-queue")
	util.CrashOnError(err, "Failed to create RabbitMQ queue")

	err = bindQueueToExchange(rabbitMQQueue, rabbitMQChannel, "property.tenant.created")
	util.CrashOnError(err, "Failed to create RabbitMQ queue")

	return &RabbitMQ{
		Conn:    rabbitMQConn,
		Channel: rabbitMQChannel,
		Queue:   rabbitMQQueue,
	}, nil
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

func createQueue(ch *amqp.Channel, queueName string) (amqp.Queue, error) {
	q, err := ch.QueueDeclare(
		queueName,
		true,
		false,
		false,
		false,
		nil,
	)

	if err != nil {
		return amqp.Queue{}, err
	}

	return q, err
}

func bindQueueToExchange(queue amqp.Queue, queueChannel *amqp.Channel, exchangeName string) error {
	return queueChannel.QueueBind(
		queue.Name,
		"#", //routing key wildcard (accept everything from this exchange)
		exchangeName,
		false,
		nil,
	)
}
