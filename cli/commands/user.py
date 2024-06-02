from typing import Set

import registry
from client.HttpRequest import HttpMethod
from manager.ICommand import ICommand


def register():
    registry.register(GetSelf())
    registry.register(Get())
    registry.register(Update())
    registry.register(UpdateSelf())
    registry.register(ListAccounts())
    registry.register(ListGoals())
    registry.register(ListBudgets())
    registry.register(ListTransactions())


class GetSelf(ICommand):

    def get_name(self) -> str:
        return 'get-self'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/user'

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class Get(ICommand):
    def get_name(self) -> str:
        return 'get-user'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/user/{id}'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class Update(ICommand):
    def get_name(self) -> str:
        return 'upd-user'

    def get_method(self) -> HttpMethod:
        return HttpMethod.PATCH

    def get_url(self) -> str:
        return '/user/{id}'

    def get_body_params(self) -> Set[str]:
        return {'login', 'password'}

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class UpdateSelf(ICommand):
    def get_name(self) -> str:
        return 'upd-self'

    def get_method(self) -> HttpMethod:
        return HttpMethod.PATCH

    def get_url(self) -> str:
        return '/user'

    def get_body_params(self) -> Set[str]:
        return {'login', 'password'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class ListAccounts(ICommand):
    def get_name(self) -> str:
        return 'list-acc'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/user/{id}/accounts'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class ListBudgets(ICommand):
    def get_name(self) -> str:
        return 'list-budg'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/user/{id}/budgets'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class ListTransactions(ICommand):
    def get_name(self) -> str:
        return 'list-trs'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/user/{id}/transactions'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}


class ListGoals(ICommand):
    def get_name(self) -> str:
        return 'list-goals'

    def get_method(self) -> HttpMethod:
        return HttpMethod.GET

    def get_url(self) -> str:
        return '/user/{id}/goals'

    def get_path_params(self) -> Set[str]:
        return {'id'}

    def get_headers(self) -> Set[str]:
        return {'Authorization'}
