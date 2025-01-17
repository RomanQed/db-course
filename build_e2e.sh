#!/bin/sh

run_script() {
  tr -d '\015' <"$1" >"$1".tmp
  mv "$1".tmp "$1"
  chmod +x "$1"
  ./"$1" "$2"
}

rm auth.json
run_script conf_auth.sh --default
rm -rf ./build
rm -rf ./e2e/build
rm salt.pbkdf2
rm postgres.json
rm server.json
cp e2e/postgres.json postgres.json
cp e2e/server.json server.json
gradle e2e:shadowJar