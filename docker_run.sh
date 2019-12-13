#!/usr/bin/env sh

GENERATED_CSV="${MIN_MACH}-${MAX_MACH}-machines.csv"

echo "Outputting to $GENERATED_CSV"

CONTAINER_NAME_AND_TAG="aiad-${MIN_MACH}-${MAX_MACH}"

docker build -t "$CONTAINER_NAME_AND_TAG" .
# Because of https://stackoverflow.com/a/47099098/5437511

touch "logs/$GENERATED_CSV"

# Use -e ENV_VAR=value to override values in create_dataset.js
# Using a volume so that the csv sync to the host. Could also copy but that requires the container to be running and it is not when it is done
docker run --rm -v "$(pwd)/logs/$GENERATED_CSV:/aiad/logs/$GENERATED_CSV" --name "$CONTAINER_NAME_AND_TAG" -e GENERATED_CSV="$GENERATED_CSV" -e MIN_MACH="$MIN_MACH" -e MAX_MACH="$MAX_MACH" -it "$CONTAINER_NAME_AND_TAG"


# MIN_MACH=2 MAX_MACH=4 ./docker_run.sh

# MIN_MACH=5 MAX_MACH=7 ./docker_run.sh

# MIN_MACH=8 MAX_MACH=10 ./docker_run.sh
