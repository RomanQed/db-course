from typing import Set

import registry
from client.HttpRequest import HttpMethod
from manager.ICommand import ICommand


def register():
    registry.register(Get())
    registry.register(Put())
    registry.register(Delete())


class Get(ICommand):

    def get_name(self) -> str:
        return 'get-trs'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/transaction/{id}'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class Put(ICommand):
    def get_name(self) -> str:
        return 'put-trs'

    def get_method(self) -> HttpMethod:
        return HttpMethod.PUT

    def get_url(self) -> str:
        return '/transaction'

    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_body_params(self) -> Set[str]:
        return {'category', 'from', 'to', 'value', 'description'}


class Delete(ICommand):
    def get_name(self) -> str:
        return 'del-trs'

    def get_method(self) -> HttpMethod:
        return HttpMethod.DELETE

    def get_url(self) -> str:
        return '/transaction/{id}'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}
