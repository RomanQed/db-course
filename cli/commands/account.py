from typing import Set

import registry
from client.HttpRequest import HttpMethod
from manager.ICommand import ICommand


def register():
    registry.register(Get())
    registry.register(Put())
    registry.register(Update())
    registry.register(Delete())


class Get(ICommand):
    def get_name(self) -> str:
        return 'get-acc'

    def get_url(self) -> str:
        return '/account/{id}'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET


class Put(ICommand):
    def get_name(self) -> str:
        return 'put-acc'

    def get_url(self) -> str:
        return '/account'

    def get_method(self) -> HttpMethod:
        return HttpMethod.PUT

    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_body_params(self) -> Set[str]:
        return {'currency', 'description', 'value'}


class Update(ICommand):
    def get_name(self) -> str:
        return 'upd-acc'

    def get_url(self) -> str:
        return '/account/{id}'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.PATCH

    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_body_params(self) -> Set[str]:
        return {'currency', 'description'}


class Delete(ICommand):
    def get_name(self) -> str:
        return 'del-acc'

    def get_url(self) -> str:
        return '/account/{id}'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.DELETE

    def get_headers(self) -> Set[str]:
        return {'Authorization'}
