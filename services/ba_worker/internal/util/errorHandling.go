package util

import (
	"errors"
	"log"
	"net"
)

func CrashOnError(err error, msg string) {
	if err != nil {
		log.Panicf("%s, %s", err, msg)
	}
}

func IsTransient(err error) bool {
	// 1. Check if it's a network error (Timeout, DNS failure, etc.)
	var netErr net.Error
	if errors.As(err, &netErr) {
		return netErr.Timeout() || netErr.Temporary()
	}

	return false
}
