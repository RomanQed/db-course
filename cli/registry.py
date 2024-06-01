from manager.ICommand import ICommand
from manager.ManagerBuilder import ManagerBuilder

_registry = list()


def reset():
    _registry.clear()


def register(command: ICommand):
    _registry.append(command)


def add_to_builder(builder: ManagerBuilder):
    global _registry
    to_set = _registry
    _registry = list()
    builder.set_commands(to_set)
