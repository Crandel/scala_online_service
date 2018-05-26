package test

case class User(username: String, password: String, subscribe: Boolean = false)

case class Role(user: User, roleType: String)

case class Table(name: String, participants: Seq[User])
