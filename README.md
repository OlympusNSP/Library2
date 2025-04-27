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

Для сборки книг в библиотеке
PUT /order/orderbook/[номер orderBook] {"status":"PREPARED"}
Для выдачи книг
PUT /order/orderbook/[номер orderBook] {"status":"RENTED"}

Для того, чтобы книги вернуть
PUT /order/orderbook/[номер orderBook] {"status":"RETURNED"}
