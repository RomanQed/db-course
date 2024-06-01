import json
import os.path
import sys

import registry
from client.HttpResponse import HttpResponse
from manager.Manager import Manager
from manager.ManagerBuilder import ManagerBuilder
from req.RequestsHttpClient import RequestsHttpClient


def print_dict(e):
    for k, v in e.items():
        print(k + ': ' + str(v))


def print_json(e):
    if isinstance(e, dict):
        print_dict(e)
        return
    if isinstance(e, list):
        for item in e:
            print('===')
            print_json(item)
        return
    print(e)


def output(response: HttpResponse):
    print('Status: ' + str(response.get_status()))
    body = response.get_body()
    print_json(body)


def build(builder: ManagerBuilder, token, args) -> Manager:
    # Set host
    host = args.get('host')
    if host is None:
        host = 'http://127.0.0.1:8080'
    builder.set_host(host)
    # Set http client
    client = RequestsHttpClient()
    builder.set_client(client)
    # Set printer
    builder.set_printer(output)

    # Set source
    def source(name):
        ret = args.get(name)
        if ret is None and name == 'Authorization':
            return token
        return ret

    builder.set_source(source)

    # Register commands
    registry.add_to_builder(builder)
    return builder.build()


def parse_named(args):
    ret = dict()
    for arg in args:
        if not arg.startswith('--'):
            continue
        index = arg.find('=')
        if index < 0:
            ret[arg[2:]] = None
        else:
            value = arg[index + 1:]
            if value.startswith('[') and value.endswith(']'):
                value = json.loads(value)
            ret[arg[2:index]] = value
    return ret


TOKEN_FILE = 'token.txt'


def read_token():
    if not os.path.exists(TOKEN_FILE):
        return None
    try:
        with open(TOKEN_FILE, 'r') as f:
            return 'Bearer ' + f.readline().strip()
    except:
        return None


def main(args):
    if len(args) == 0:
        print('Missing command')
        return 1
    token = read_token()
    builder = ManagerBuilder()
    manager = build(builder, token, parse_named(args[1:]))
    return 0 if manager.execute(args[0]) else 1


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
