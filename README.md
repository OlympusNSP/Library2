# Онлайн библиотека


## API 
Port 8081
/swagger-ui/index.html

## Авторизация
Авторизация использует JWT токен, 
необходимо зарегистрироваться
/auth/sign-up
Сделано по примеру
https://habr.com/ru/articles/784508/
Получившийся токен добавляем в Bearer.

Затем, заказ делается через
POST /order 
{"user_id":номер пользователя,"book_ids":[список id книг]}

Для выдачи книг
POST /order/[номер заказа]/start

Для того, чтобы отдать книги, надо
POST /book/[ИД книги]/return
{"user_id:number, book_id:number}