import de.msiemens.db.DB

fun main() {
    val db = DB()

    println(db.exec("create table names(id integer, value string)"))

    println(db.exec("show tables"))
    println(db.exec("show index from names"))

    println(db.exec("insert into names values(1, \"John\")"))
    println(db.exec("insert into names values(2, \"Jane\")"))
    println(db.exec("select value from names where id = 1"))
    println(db.exec("select id from names where value = \"Jane\""))
    println(db.exec("select id from names where value like \"Ja%\""))
    println(db.exec("select * from names order by id desc"))

    println(db.exec("create index id on names (id)"))
    println(db.exec("show index from names"))
    println(db.exec("select value from names where id = 1"))
    println(db.exec("select value from names where id = 2"))

    println(db.exec("drop table names"))
}
