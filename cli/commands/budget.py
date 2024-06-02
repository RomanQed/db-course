from typing import Set

import registry
from client.HttpRequest import HttpMethod
from manager.ICommand import ICommand


def register():
    registry.register(Status())
    registry.register(Get())
    registry.register(Put())
    registry.register(Update())
    registry.register(Delete())


class Status(ICommand):

    def get_name(self) -> str:
        return 'stat-budg'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/budget/{id}/status'

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class Get(ICommand):
    def get_name(self) -> str:
        return 'get-budg'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/budget/{id}'

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class Put(ICommand):
    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_name(self) -> str:
        return 'put-budg'

    def get_body_params(self) -> Set[str]:
        return {'currency', 'start', 'end', 'description', 'value'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.PUT

    def get_url(self) -> str:
        return '/budget'


class Update(ICommand):
    def get_name(self) -> str:
        return 'upd-budg'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.PATCH

    def get_url(self) -> str:
        return '/budget/{id}'

    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_body_params(self) -> Set[str]:
        return {'currency', 'description', 'value'}


class Delete(ICommand):
    def get_name(self) -> str:
        return 'del-budg'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.DELETE

    def get_url(self) -> str:
        return '/budget/{id}'

    def get_headers(self) -> Set[str]:
        return {'Authorization'}
