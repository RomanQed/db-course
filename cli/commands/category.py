from typing import Set

import registry
from client.HttpRequest import HttpMethod
from manager.ICommand import ICommand


def register():
    registry.register(ListTransactions())
    registry.register(Get())
    registry.register(Find())
    registry.register(Put())
    registry.register(Update())
    registry.register(Delete())


class ListTransactions(ICommand):
    def get_name(self) -> str:
        return 'list-cat'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/category/{id}/transactions'

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class Get(ICommand):

    def get_name(self) -> str:
        return 'get-cat'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/category/{id}'


class Find(ICommand):
    def get_name(self) -> str:
        return 'find-cat'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/category'

    def get_queries(self) -> Set[str]:
        return {'name'}


class Put(ICommand):
    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_name(self) -> str:
        return 'put-cat'

    def get_method(self) -> HttpMethod:
        return HttpMethod.PUT

    def get_url(self) -> str:
        return '/category'

    def get_body_params(self) -> Set[str]:
        return {'name'}


class Update(ICommand):
    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_name(self) -> str:
        return 'upd-cat'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.PATCH

    def get_url(self) -> str:
        return '/category/{id}'

    def get_body_params(self) -> Set[str]:
        return {'name'}


class Delete(ICommand):
    def get_headers(self) -> Set[str]:
        return {'Authorization'}

    def get_name(self) -> str:
        return 'del-cat'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_method(self) -> HttpMethod:
        return HttpMethod.DELETE

    def get_url(self) -> str:
        return '/category/{id}'
