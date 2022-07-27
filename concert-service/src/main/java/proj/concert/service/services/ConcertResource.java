package proj.concert.service.services;
import org.slf4j.LoggerFactory;
import proj.concert.common.dto.*;
import proj.concert.common.types.BookingStatus;
import proj.concert.service.domain.*;
import proj.concert.service.jaxrs.LocalDateTimeParam;
import proj.concert.service.mapper.BookingMapper;
import proj.concert.service.mapper.ConcertMapper;
import proj.concert.service.mapper.PerformerMapper;
import proj.concert.service.mapper.SeatMapper;
import proj.concert.service.util.TheatreLayout;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

@Path("/concert-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConcertResource {
    private static final ConcurrentHashMap<LocalDateTime, LinkedList<Subscriber>> subscriptionsList = new ConcurrentHashMap<>();
    private static final ExecutorService thread = Executors.newSingleThreadExecutor();
    public static final String user_authorization_cookie = "auth";
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
    private User newUser;
    private LocalDateTime date;

    //Counts free seats, returns array list
    public List<Seat> checkFreeSeatList(EntityManager entityManager, BookingRequestDTO newBookingRequest) {
        LOGGER.info("Counting free seats for booking and return arraylist");
        return (List<Seat>) entityManager.createQuery("select s from Seat s where s.label in :label and s.date = :date and s.seatReserved = false", Seat.class)
                    .setParameter("label", newBookingRequest.getSeatLabels())
                    .setParameter("date", newBookingRequest.getDate())
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();
    }

    //Counts free seats, return integer value
    public int checkFreeSeatValue(EntityManager entityManager, BookingRequestDTO newBookingRequest) {
        LOGGER.info("Counting free seats for booking and return integer value");
        return entityManager.createQuery("select count(nSeat) from Seat nSeat where nSeat.date = :date and nSeat.seatReserved = false", Long.class)
                .setParameter("date", newBookingRequest.getDate())
                .getSingleResult()
                .intValue();
    }

    //Get new cookie with USER and Entity manager specfic bound to it
    //Give user id and commit to database
    public NewCookie newSession(User user, EntityManager entityManager) {
        entityManager.getTransaction().begin();
        user.setSessionId(UUID.randomUUID());
        entityManager.lock(user, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        entityManager.getTransaction().commit();
        String toReturnUser = user.getSessionId().toString();
        NewCookie newUserCookie = new NewCookie(user_authorization_cookie, toReturnUser);
        return newUserCookie;
    }

    // Validates the date for a booking, return a boolean result
    public boolean validateDate(EntityManager entityManager, String newDate) {
        return entityManager.createQuery("select count(nSeat) FROM Seat nSeat WHERE nSeat.date = :date", Long.class).setParameter("date", new LocalDateTimeParam(newDate).getLocalDateTime())
        .getSingleResult()
        .intValue() > 0;
    }

    // Gets active user
    // helps make sure that some methods are interacting with a registered user
    public User activeUser(Cookie userCookie, EntityManager entityManager) {
        if (userCookie != null) {
            User locUser = null;
            try {
                entityManager.getTransaction().begin();
                try {
                    locUser = entityManager.createQuery("select e from User e where e.sessionId = :uuid", User.class).setParameter("uuid", UUID.fromString(userCookie.getValue())).setLockMode(LockModeType.OPTIMISTIC).getSingleResult();
                } catch (Exception ignored) {
                }
            } finally {
                entityManager.getTransaction().commit();
            }
            return locUser;
        } else {
            return null;
        }
    }

    //Notify subscribed users and remove them
    public void trySubmitThread(double unavail, ExecutorService testThread, LocalDateTime date, int open) {
        thread.submit(() -> {
            for (Iterator<Subscriber> iterator = subscriptionsList.get(date).iterator(); iterator.hasNext(); ) {
                Subscriber sub = iterator.next();
                if (unavail >= sub.value) {
                    iterator.remove();
                    sub.concert.resume(Response.ok(new ConcertInfoNotificationDTO(open)).build());
                }
            }
        });
    }

    public boolean getLocSeatsValidity(List<Seat> locSeats, BookingRequestDTO newBookingRequest) {
        return !(locSeats.size() == newBookingRequest.getSeatLabels().size());
    }

    //Retrieve subscribed users
    private void subscribersCall(LocalDateTime date, int open) {
        LOGGER.info("Checking the subscribers for EM");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        ExecutorService thread = Executors.newSingleThreadExecutor();
        LOGGER.info("Get seat calcualation");
        double unavail = getUnavailable(open);
        LOGGER.info("Attempt thread submission");
        trySubmitThread(unavail, thread, date, open);
    }

    //Login user, check their entered credentials against database
    //if not match return UNAUTHORIZED
    @POST
    @Path("/login")
    public Response userLogin(UserDTO attempt) {
        LOGGER.info("Authenticating user");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            try {
                newUser = entityManager.createQuery("select n from User n where n.username = :username and n.password = :password", User.class)
                        .setParameter("username", attempt.getUsername()).setParameter("password", attempt.getPassword()).setLockMode(LockModeType.PESSIMISTIC_READ).getSingleResult();
            } catch (Exception ex) {
                Response.ResponseBuilder con = Response.status(Response.Status.UNAUTHORIZED);
                LOGGER.info("Unauthorized user");
                return con.build();
            } finally {
                entityManager.getTransaction().commit();
            }
            Response.ResponseBuilder res = Response.ok().cookie(newSession(newUser, entityManager));
            LOGGER.info("Successfully authenticated user");
            return res.build();
        } finally {
            LOGGER.info("Closing EM");
            entityManager.close();
        }
    }

    //Return unavailable seats
    public double getUnavailable(int open) {
        return 1.0 - open / (double) TheatreLayout.NUM_SEATS_IN_THEATRE;
    }

    //Run subscription check on list
    public void checkSubsList(ConcertInfoSubscriptionDTO sub, @Suspended AsyncResponse response) {
        synchronized (subscriptionsList) {
            if (subscriptionsList.contains(sub.getDate())) {
                subscriptionsList.get(sub.getDate()).add(new Subscriber(response, sub.getPercentageBooked()));
            } else {
                subscriptionsList.put(sub.getDate(), new LinkedList<>());
            }
            subscriptionsList.get(sub.getDate()).add(new Subscriber(response, sub.getPercentageBooked()));
        }
    }

    //Helper class to store Subscribers with taken seat target percentahe
    class Subscriber {
        public final AsyncResponse concert;
        public final double value;

        public Subscriber(AsyncResponse concert, int value) {
            this.concert = concert;
            this.value = value / 100.0;
        }
    }

    //Retrieve all Concerts
    @GET
    @Path("/concerts")
    public Response retrieveAllConcerts() {
        LOGGER.info("Retrieving concerts from database");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        //Query, All concerts
        //Allow for others to read but,
        //No writing should occur while reading
        System.out.println("TEST does this get printed");
        try {
            entityManager.getTransaction().begin();
            List<Concert> concertList = entityManager
                    .createQuery("select Nconcert from Concert Nconcert", Concert.class).setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
            System.out.println("TEST does this get printed");
            System.out.println(concertList);
            //Empty list, No Content Response
            if (concertList.isEmpty()) {
                Response.ResponseBuilder fail = Response.noContent();
                LOGGER.info("Retrieving concerts from database failed");
                return fail.build();
            }
            // Add to all concerts to list
            Set<ConcertDTO> concertDTOS = new HashSet<>();
            for (Concert c : concertList) {
                concertDTOS.add(ConcertMapper.concertMapperDTO(c));
            }
            entityManager.getTransaction().commit();
            //Collate concerts in set and place in response
            GenericEntity<Set<ConcertDTO>> test = new GenericEntity<Set<ConcertDTO>>(concertDTOS) {
            };
            Response.ResponseBuilder connect = Response.ok(test);
            LOGGER.info("Retrieving concerts from success");
            System.out.println("TEST does this get printed");
            return connect.build();
        } finally {
            //End transactions
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Committing to EM on success");
            }
            LOGGER.info("Closing EM");
            entityManager.close();
        }
    }

    //Get ID specific Concert and return
    @GET
    @Path("/concerts/{id}")
    public Response retrieveConcert(@PathParam("id") long id) {
        LOGGER.info("Retrieving concerts(id) from database");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        try {
            //Query, Find id matched concert
            entityManager.getTransaction().begin();
            Concert concert = entityManager.find(Concert.class, id, LockModeType.PESSIMISTIC_READ);
            System.out.println("TEST does this get printed");
            //Null concert, not found response
            if (concert == null) {
                Response.ResponseBuilder fail = Response.status(Response.Status.NOT_FOUND);
                LOGGER.info("Unsuccessfully retrieved concert(s)");
                return fail.build();
            }
            //Found concert returned back
            Response.ResponseBuilder con = Response.ok(ConcertMapper.concertMapperDTO(concert));
            LOGGER.info("Successfully retrieved concert(s)");
            return con.build();
        } finally {
            //End transactions
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Successfully retrieved concert(s) and commited to EM");
            }
            System.out.println("TEST does this get printed");
            entityManager.close();
            LOGGER.info("Successfully closed EM");
        }
    }

    //Get all Summaries for concerts
    @GET
    @Path("/concerts/summaries")
    public Response getSummary() {
        LOGGER.info("Retrieving summaries from database");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        Set<ConcertSummaryDTO> SummaryDTOSet = new HashSet<>();
        try {
            // Obtain all concerts first
            entityManager.getTransaction().begin();
            List<Concert> concertList = entityManager.createQuery("select nConcert from Concert nConcert", Concert.class).setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
            if (concertList.isEmpty()) {
                // Empty list, No COntent Response returned
                Response.ResponseBuilder fail = Response.noContent();
                LOGGER.info("Unsuccessfully retrieved summaries");
                return fail.build();
            }
            //Collect all summaries from all Concerts
            for (Concert c : concertList) {
                SummaryDTOSet.add(ConcertMapper.summaryDTO(c));
            }
            //Collate and return all summaries
            GenericEntity<Set<ConcertSummaryDTO>> concertSummary = new GenericEntity<Set<ConcertSummaryDTO>>(SummaryDTOSet) {
            };
            entityManager.getTransaction().commit();
            Response.ResponseBuilder con = Response.ok(concertSummary);
            LOGGER.info("Successfully retrieved summaries");
            return con.build();
        } finally {
            //End Transactions
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Successfully retrieved summaries and commited to EM");
            }
            System.out.println("TEST does this get printed");
            entityManager.close();
            LOGGER.info("Successfully closed EM");
        }
    }

    //Get All Performers
    @GET
    @Path("/performers")
    public Response getPerformers() {
        LOGGER.info("Retrieving performers from database");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        try {
            // Query all performers in database to return in response
            entityManager.getTransaction().begin();
            List<Performer> performers = entityManager.createQuery("select nPerfromer from Performer nPerfromer", Performer.class).setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
            // Empty list, No Content response
            if (performers.isEmpty()) {
                Response.ResponseBuilder con = Response.noContent();
                LOGGER.info("Unsuccessfully retrieved performer(s)");
                return con.build();
            }
            //Collate and return performers in return
            Set<PerformerDTO> performerDTOSet = new HashSet<>();
            for (Performer p : performers) {
                performerDTOSet.add(PerformerMapper.PerformerMapperDTO(p));
            }
            System.out.println("TEST does this get printed");
            entityManager.getTransaction().commit();
            GenericEntity<Set<PerformerDTO>> res = new GenericEntity<>(performerDTOSet) {
            };
            Response.ResponseBuilder output = Response.ok(res);
            LOGGER.info("Successfully retrieved performer(s)");
            return output.build();
        } finally {
            //end transaction
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Successfully retrieved performers and commited to EM");
            }
            System.out.println("TEST does this get printed");
            entityManager.close();
            LOGGER.info("Successfully closed EM");
        }
    }

    //Get ID specific Performers
    @GET
    @Path("/performers/{id}")
    public Response getPerformer(@PathParam("id") long id) {
        LOGGER.info("Retrieving performers from database");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            // Find matching id Performer in table
            // None can write
            // only read
            Performer performer = entityManager.find(Performer.class, id, LockModeType.PESSIMISTIC_READ);
            System.out.println("TEST does this get printed");
            //null performer, not found response
            if (performer == null) {
                Response.ResponseBuilder con = Response.status(Response.Status.NOT_FOUND);
                LOGGER.info("Unsuccessfully retrieved performer(s)");
                return con.build();
            } else {
                //found return performer
                Response.ResponseBuilder res = Response.ok(PerformerMapper.PerformerMapperDTO(performer));
                LOGGER.info("Successfully retrieved performer(s)");
                return res.build();
            }
        } finally {
            //end transaction
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Successfully retrieved performers and commited to EM");
            }
            System.out.println("TEST does this get printed");
            entityManager.close();
            LOGGER.info("Successfully closed EM");
        }
    }


     // getSeatStatus takes a specified date and a potential status, checks if the date is valid. if not, return a 400 response
     // if it is, then carry on and return the seats for that date pertaining to the specified status.
    @GET
    @Path("/seats/{date}")
    public Response getSeatStatus(@PathParam("date") String newDate, @DefaultValue("Any") @QueryParam("status") BookingStatus bookStatus) {
        date = new LocalDateTimeParam(newDate).getLocalDateTime();
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        entityManager.getTransaction().begin();

        // if date is not valid, return 400, else continue
        try {
            if(validateDate(entityManager, newDate) == false) {
                Response.ResponseBuilder con = Response.status(Response.Status.BAD_REQUEST);
                LOGGER.info("Seat not available/booked");
                return con.build();
            }

            // date is valid so return seats according to date and status.
            TypedQuery<Seat> dbSearch;
            TypedQuery<Seat> anyStat = entityManager.createQuery("select nSeat from Seat nSeat where nSeat.date = :date", Seat.class)
                    .setParameter("date", date);
            TypedQuery<Seat> specificStat = entityManager.createQuery("select nSeat from Seat nSeat where nSeat.date = :date and nSeat.seatReserved = :status", Seat.class)
                    .setParameter("date", date)
                    .setParameter("status", bookStatus == BookingStatus.Booked);
            if (bookStatus == BookingStatus.Any) {
                dbSearch = anyStat;
            } else {
                dbSearch = specificStat;
            }
            //takes the returned seats and converts to dtos
            List<Seat> queryToSeatList = dbSearch.setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
            Set<SeatDTO> seatDtoConvertSet = new HashSet<>();
            for (Seat s : queryToSeatList) {
                seatDtoConvertSet.add(SeatMapper.seatMapperDTO(s));
            }

            GenericEntity<Set<SeatDTO>> convertSeat = new GenericEntity<Set<SeatDTO>>(seatDtoConvertSet) {
            };
            //return seat status, OK response
            Response.ResponseBuilder res = Response.ok(convertSeat);
            LOGGER.info("Successfully retrieved seat status for user for DB");
            return res.build();
        } finally {
            //end transaction
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Successfully retrieved seat status for user and commited to EM");
            }
            System.out.println("TEST does this get printed");
            entityManager.close();
            LOGGER.info("Successfully closed EM");
        }
    }

    //Get specific ID booking
    @GET
    @Path("/bookings/{id}")
    public Response getBooking(@PathParam("id") Long bookingID, @CookieParam(user_authorization_cookie) Cookie cookie) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        Booking foundBooking;

        try {
            User user = activeUser(cookie, entityManager);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            entityManager.getTransaction().begin();
            foundBooking = entityManager.find(Booking.class, bookingID, LockModeType.PESSIMISTIC_READ);

            // if users is not same as the found booking(user) then forbidden
            if ((foundBooking.getUser().equals(user)) == false) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }


        } finally {
            // end transaction
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        return Response.ok(BookingMapper.bookingMapperDTO(foundBooking))
                .build();
    }

    /**
     * Takes a users, returns the users bookings
     * if user is not logged in, return unauthorized.
     */
    @GET
    @Path("/bookings")
    public Response getUsersBookings(@CookieParam(user_authorization_cookie) Cookie authCookie) {
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();

        try {
            LOGGER.info("Checking if user is authorized");
            User user = activeUser(authCookie, entityManager);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            LOGGER.info("User is authorized");
            entityManager.getTransaction().begin();
            entityManager.lock(user, LockModeType.OPTIMISTIC);

            Set<BookingDTO> bookingsDTOSET = new HashSet<>();
            for (Booking b : user.getUserBookings()) {
                bookingsDTOSET.add(BookingMapper.bookingMapperDTO(b));
            }

            entityManager.getTransaction().commit();

            GenericEntity<Set<BookingDTO>> out = new GenericEntity<Set<BookingDTO>>(bookingsDTOSET) {

            };
            LOGGER.info("Returned the user's booking(s)");
            return Response.ok(out).build();
        } finally {

            entityManager.close();
        }
    }

    //Subscribe users to Concerts
    @POST
    @Path("/subscribe/concertInfo")
    public void subscribeToConcert(@Suspended AsyncResponse response, @CookieParam(user_authorization_cookie) Cookie authCookie, ConcertInfoSubscriptionDTO sub) {
        LOGGER.info("Retrieving user concert subscription data");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        User getUser = activeUser(authCookie, entityManager);
        try {
            //cant have a null user subscribing to concerts
            if (getUser == null) {
                Response.ResponseBuilder con = Response.status(Response.Status.UNAUTHORIZED);
                boolean res = response.resume(con.build());
                LOGGER.info("Unsuccessfull");
                return;
            }
            entityManager.getTransaction().begin();
            Concert extendConcert = entityManager.find(Concert.class, sub.getConcertId(), LockModeType.PESSIMISTIC_READ);
            if (extendConcert == null) {
                Response.ResponseBuilder conResume = Response.status(Response.Status.BAD_REQUEST);
                boolean resResume = response.resume(conResume.build());
                LOGGER.info("Successful");
                return;
            }
            if (!extendConcert.getConcertDates().contains(sub.getDate())) {
                Response.ResponseBuilder conResume = Response.status(Response.Status.BAD_REQUEST);
                boolean resResume = response.resume(conResume.build());
                LOGGER.info("Successful");
                return;
            }
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Successfully retrieved subscription data");
            }
            System.out.println("TEST does this get printed");
            entityManager.close();
            LOGGER.info("Successfully closed EM");
        }
        checkSubsList(sub, response);
    }

     //Method for creating a booking, uses auth cookie and BookingRequestDto
    @POST
    @Path("/bookings")
    public Response createBooking(BookingRequestDTO newBookingRequest, @CookieParam(user_authorization_cookie) Cookie userCookie) {
        LOGGER.info("Retrieving user concert subscription data");
        EntityManager entityManager = PersistenceManager.instance().createEntityManager();
        User getUser = activeUser(userCookie, entityManager);
        entityManager.getTransaction().begin();
        Concert locConcert = entityManager.find(Concert.class, newBookingRequest.getConcertId(), LockModeType.PESSIMISTIC_READ);
        try {
            if (getUser == null) {
                Response.ResponseBuilder con = Response.status(Response.Status.UNAUTHORIZED);
                LOGGER.info("Unsuccessful in retrieving user");
                return con.build();
            }
            if (locConcert == null) {
                Response.ResponseBuilder conRequest = Response.status(Response.Status.BAD_REQUEST);
                LOGGER.info("Unsuccessful as Concert is null");
                return conRequest.build();
            }
            if (!locConcert.getConcertDates().contains(newBookingRequest.getDate())) {
                Response.ResponseBuilder conResume = Response.status(Response.Status.BAD_REQUEST);
                LOGGER.info("Unsuccessful as does not contain date");
                return conResume.build();
            }
            List<Seat> locSeats = checkFreeSeatList(entityManager, newBookingRequest);
            if (getLocSeatsValidity(locSeats, newBookingRequest)) {
                Response.ResponseBuilder forbiddenRes = Response.status(Response.Status.FORBIDDEN);
                LOGGER.info("Unsuccessful due to size mismatch/inconsistency");
                return forbiddenRes.build();
            }
            Booking validatedBooking = new Booking(getUser, newBookingRequest.getConcertId(), newBookingRequest.getDate());
            validatedBooking.getSeat().addAll(locSeats);
            for (Seat s: validatedBooking.getSeat()) {
                s.setSeatReserved(true);
            }
            entityManager.persist(validatedBooking);
            int freeSeats = checkFreeSeatValue(entityManager, newBookingRequest);
            subscribersCall(newBookingRequest.getDate(), freeSeats);
            URI URIResAddr = URI.create("/concert-service/bookings/" + validatedBooking.getBookingId());
            Response.ResponseBuilder validRes = Response.created(URIResAddr);
            LOGGER.info("Successfully created booking!!!");
            return validRes.build();
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
                LOGGER.info("Successfully retrieved subscription data");
            }
            System.out.println("TEST does this get printed");
            entityManager.close();
            LOGGER.info("Successfully closed EM");
        }
    }
}
