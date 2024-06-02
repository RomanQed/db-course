from typing import Set

import registry
from client.HttpRequest import HttpMethod
from client.HttpResponse import HttpResponse
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

    def after(self, response: HttpResponse):
        if response.get_status() != 200:
            return
        with open('token.txt', 'w') as token_file:
            token_file.write(response.get_body().get('token'))


class Register(ICommand):
    def get_name(self) -> str:
        return 'register'

    def get_url(self) -> str:
        return '/register'

    def get_method(self) -> HttpMethod:
        return HttpMethod.POST

    def get_body_params(self) -> Set[str]:
        return {'login', 'password'}

    def after(self, response: HttpResponse):
        if response.get_status() != 200:
            return
        with open('token.txt', 'w') as token_file:
            token_file.write(response.get_body().get('token'))
