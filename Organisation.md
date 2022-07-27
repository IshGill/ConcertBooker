## Organisation.md

Leo Wood - github leo-wood
Ish Gill - github ishgill
Roshan Patel - github rpat371

We did initial communications on the github repo, but we moved communication to discord as it was easier to "ping" members.
We regularly caught up on a discord call to catch each other up on our progress and whether anyone needed help with their task.
We initially delegated tasks on github discussions but quickly realised our task delegation wasn't quite efficient.


Initially -
Leo tasked with:
created first draft of domain models
annotated DTOs
did some mappers


Ish tasked with:
Concert Resource
did some mappers
checked/helped with domain model associations


Roshan tasked with:
Concert Resource

On the final sunday:
We had too many errors so we used a code collaboration tool online, put our code there and divvied it up to,
Leo did organisation md, domain models, a few ConcertResource methods
Ish did large majority of concertResource
Roshan went through and added comments to ensure code was well-commented
this meant that on sunday we made one final commit from one person (Ish's account) that includes the whole assigment as our final attempt at.


### concurrency errors

To minimise chances of concurrency errors, we employed strategies such as setting lockmode type to Pessmistic read/write when we are querying the database.
We set lockmode type to Pessimisic read/write when we needed to lock the entity at the "database level" to ensure integrity of our entity and we werent worried about deadlocks.
We used optimistic read when we weren't worried about concurrent update queries altering the @version number. 


### Domain Model Organisation

We used a One to many association to link our User domain model to our Booking domain model, respectively.
We used a many to many association between performers and concerts.
Finally we needed a composite primary key for seat using the seats label and date. This provided a PK for seat and allowed for Booking to have a One to many association with seats.

