# dto-utils

## Objective

The idea is to be able to automatically transform data structures retrieved from
a DB into object implementing DTO-like interfaces. This is the quite narrow
use case for this library.

## Example

Let's imagine we have the following Java interfaces

```java

package dto.api;

public interface IAddress {
    String getStreet();
    String getNumber();
}

public interface IPerson {
    String getName();
    String getSurName();
    IAddress getAddress();
    IAddress [] getOtherAddresses();
    String [] getAliases();
}

```

And let's imagine that we have a service like this:

```java

public interface IPersonService {
    IPerson getPersonById(String id);
}

```

The objective is to be able to implement the service in clojure without having
to implement the IPerson or IAddress interfaces.

The clojure code we look for is something like:

```clojure

(defn -getPersonById [id]
  (let [person (retrieve-person-from-db id)]
    (map->dto person dto.api.IPerson)))

```

Or, if using something like korma

```clojure

(defn -getPersonById [id]
  (map->dto (select persons (with address)
                            (with other-addresses)
                            (with aliases)
                            (where :id id))
            dto.api.IPerson))

```

Easy.


## Examples
With the interfaces IPerson, IAddress defined above the macro generates the following code:

```clojure

  (macroexpand-1 '(map->dto m dto.api.IPerson))

  ;; Example code produced by the line above
  (reify
    dto.api.IPerson
    (getOtherAddresses
        [_]
      (into-array
       dto.api.IAddress
       (clojure.core/map
        (fn*
         [p1__19790__19791__auto__]
         (dto.util/map->dto* p1__19790__19791__auto__ dto.api.IAddress))
        (:other-addresses m))))
    (getAliases [_] (into-array java.lang.String (:aliases m)))
    (getSurName [_] (:sur-name m))
    (getAddress
        [_]
      (reify
        dto.api.IAddress
        (getNumber [_] (:number (:address m)))
        (getFullAddress [_] (:full-address (:address m)))
        (getStreet [_] (:street (:address m)))))
    (getName [_] (:name m))))
```


### Bugs

- Currently, the interface to implement needs to be specified with
the full package


Copyright © 2018 Pedro Abelleia Seco

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
