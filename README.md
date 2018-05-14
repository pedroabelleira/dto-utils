# dto-utils

This library covers a very narrow use case: we want to implement in clojure a
set of services defined as Java interfaces which return data defined
as interfaces instead of DTOs. I.e., services with methods of the form:

IPerson [] getPeopleByCriteria(String criteria);

where IPerson is an interface which acts as a DTO, but read only (it
only has get\* methods). Some of those methods could return objects
(or arrays of objects) of other similar interfaces. In those cases,
we also want to implement those interfaces.

The question is, of course, why to use interfaces instead of plain
DTOs to transfer data. That's an interesting question, which won't
be responded here.

The main concrete use case is to allow quickly prototyping of DB
based services where data can be retrieved from the DB in a more of
less isomorphic form to the interfaces returned by the service. 

## Usage

Let's assume we have a map m with the data which conforms to the
service interface. Then returning an object which implements the
interface is as simple as:

...

(map->dto m IPerson)

...

This creates an object which implements the IPerson interface and
uses m as the source of data. 



## Examples

...

### Bugs

- Currently, the interface to implement needs to be specified with
the full package
- The reverse function (dto->map) doesn't work for now
- Arrays are not supported yet


Copyright © 2018 Pedro Abelleia Seco

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
