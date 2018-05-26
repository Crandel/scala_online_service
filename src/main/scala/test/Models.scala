package test

case class User(username: String, password: String, subscribe: Boolean = false)

case class Role(roleType: String)

case class Table(name: String, participants: Seq[User])
