# Payment Optimizer
Aplikacja optymalizuje sposób płatności za zamówienia w sklepie internetowym w celu maksymalizacji rabatów przy użyciu tradycyjnych metod płatności oraz punktów lojalnościowych.

---

## Autor - **Michał Ryz**


## Technologia

- Java 21
- Lombok
- Junit5
- Shadow Plugin

##  Zawartość

- Optymalizator płatności napisany w Javie 21
- Obsługa rabatów za konkretne metody płatności
- Obsługa pełnych i częściowych płatności punktami
- Budowa fat-jara przy pomocy Gradle + Shadow Plugin
- Testy jednostkowe kluczowych elementów logiki

---

##  Uruchomienie

###  Wymagania

- Java 21
- Gradle (np. `gradlew`)

---

###  Budowanie aplikacji

Aby zbudować aplikację jako fat-jar należy wykonać komendę:

```bash
./gradlew shadowJar
```

Zbudowana aplikacja została również załączona w katalogu target/app.jar

Aby uruchomić aplikację należy wykonać komendę

```
java -jar /.../target/app.jar /home/…/orders.json /home/…/paymentmethods.json
```
Gdzie /home/…/orders.json to ścieżka do pliku orders.json, a /home/…/paymentmethods.json to ścieżka do paymentmethods.json

Oba pliki zostały również załączone w katalogu /data


# Kontakt

Kontakt pod adresem mailowym - **michalryz2003@gmail.com**

