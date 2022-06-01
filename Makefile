# (C) Copyright 2022 Mario Trangoni

DOCKER_IMAGE_NAME       ?= garbagecat
DOCKER_IMAGE_TAG        ?= $(subst /,-,$(shell git rev-parse --abbrev-ref HEAD))

.PHONY: all
all: docker

.PHONY: docker
docker:
	@echo ">> building docker image"
	@docker build -t "$(DOCKER_IMAGE_NAME)" -t "$(DOCKER_IMAGE_NAME):$(DOCKER_IMAGE_TAG)" .
