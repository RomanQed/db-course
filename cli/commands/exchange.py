from typing import Set

import registry
from client.HttpRequest import HttpMethod
from manager.ICommand import ICommand


def register():
    registry.register(Get())
    registry.register(Find())
    registry.register(Put())
    registry.register(Update())
    registry.register(Delete())


class Get(ICommand):

    def get_name(self) -> str:
        return 'get-exc'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/exchange/{id}'


class Find(ICommand):
    def get_name(self) -> str:
        return 'find-exc'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/exchange'

    def get_queries(self) -> Set[str]:
        return {'from', 'to'}


class Put(ICommand):
    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_name(self) -> str:
        return 'put-exc'

    def get_method(self) -> HttpMethod:
        return HttpMethod.PUT

    def get_url(self) -> str:
        return '/exchange'

    def get_body_params(self) -> Set[str]:
        return {'from', 'to', 'factor'}


class Update(ICommand):
    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_name(self) -> str:
        return 'upd-exc'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.PATCH

    def get_url(self) -> str:
        return '/exchange/{id}'

    def get_body_params(self) -> Set[str]:
        return {'factor'}


class Delete(ICommand):
    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_name(self) -> str:
        return 'del-exc'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.DELETE

    def get_url(self) -> str:
        return '/exchange/{id}'
