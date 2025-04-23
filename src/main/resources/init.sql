create table author (
                        id serial primary key,
                        fullname varchar(255) not null
);


create table book (
                      id serial primary key,
                      title varchar(255),
                      year smallint not null,
                      description text not null,
                      count int not null,
                      available int not null,
                      reserve int not null
);


create table author_book(
                            id bigserial primary key,
                            author_id int not null,
                            book_id int not null,
                            constraint fk_author foreign key(author_id) references author(id),
                            constraint fk_book foreign key(book_id) references book(id),
                            constraint u_author_book unique (author_id,book_id)
);

create table genre (
                       id serial primary key,
                       text varchar(255) not null
);

create table genre_book(
    id bigserial primary key,
                           book_id int,
                           genre_id int,
                           constraint fk_book foreign key(book_id) references book(id),
                           constraint fk_genre foreign key(genre_id) references genre(id)
);

create table user_(
                     id serial primary key,
                     username varchar(255) not null,
                     password text not null,
                     email varchar(255) not null,
                     book_rented int not null,
                     status_block boolean not null,
                     violations int not null,
                     role varchar(20) not null
);
CREATE UNIQUE INDEX username_idx ON user_ (username);

create table rental_book(
                            id bigserial primary key,
                            user_id int not null,
                            book_id int not null,
                            date_return date,
                            date_rented_start date not null,
                            constraint fk_user foreign key(user_id) references user_(id),
                            constraint fk_book foreign key(book_id) references book(id),
                            constraint un_user_book unique (user_id,book_id)
);

create table order_
(
    id      bigserial primary key,
    user_id int,
    manager_id int,
    status  smallint not null,
    constraint fk_user foreign key (user_id) references user_ (id)
);

create table order_book
(
    id       bigserial primary key,
    order_id bigint,
    book_id  int,
    constraint fk_order foreign key (order_id) references order_ (id),
    constraint fk_book foreign key (book_id) references book (id)

);
