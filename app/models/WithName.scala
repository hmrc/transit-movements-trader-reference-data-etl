package models

class WithName(string: String) {
  override val toString: String = s"[$string]"
}