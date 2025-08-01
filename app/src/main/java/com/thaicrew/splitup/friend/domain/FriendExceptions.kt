package com.thaicrew.splitup.friend.domain

class InvalidFriendNameException(override val message: String) : Exception(message)
class FriendAlreadyExistsException(override val message: String) : Exception(message)
class FriendNotFoundException(override val message: String) : Exception(message)
class FriendAlreadyInactiveException(override val message: String) : Exception(message)