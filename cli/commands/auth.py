from typing import List, Set

import registry
from client.HttpRequest import HttpMethod
from manager.ICommand import ICommand


def register():
    registry.register(Login())
    registry.register(Register())


class Login(ICommand):

    def get_name(self) -> str:
        return 'login'

    def get_url(self) -> str:
        return '/login'

    def get_method(self) -> HttpMethod:
        return HttpMethod.POST

    def get_body_params(self) -> Set[str]:
        return {'login', 'password'}


class Register(ICommand):
    def get_name(self) -> str:
        return 'register'

    def get_url(self) -> str:
        return '/register'

    def get_method(self) -> HttpMethod:
        return HttpMethod.POST

    def get_body_params(self) -> Set[str]:
        return {'login', 'password'}
