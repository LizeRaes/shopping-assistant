
docker-login:
	docker login ghcr.io/lizeraes

install-quarkus-docker:
	quarkus extension add container-image-docker

build-docker:
	quarkus build -Dquarkus.container-image.build=true -Dquarkus.docker.additional-args="--tag=ghcr.io/lizeraes/shopping-assistant:1.0.0-SNAPSHOT"

push-docker:
	docker push ghcr.io/lizeraes/shopping-assistant:1.0.0-SNAPSHOT

build-push: build-docker push-docker

