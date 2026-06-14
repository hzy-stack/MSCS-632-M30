Object subclass: #Ride
    instanceVariableNames: 'rideID pickupLocation dropoffLocation distance'
    classVariableNames: ''
    package: 'RideSharing'.

Ride extend [
    rideID: id pickup: p dropoff: d distance: m [
        rideID := id.
        pickupLocation := p.
        dropoffLocation := d.
        distance := m.
        ^self
    ]
    rideID          [ ^rideID ]
    pickupLocation  [ ^pickupLocation ]
    dropoffLocation [ ^dropoffLocation ]
    distance        [ ^distance ]

    fare     [ ^self subclassResponsibility ]
    rideType [ ^self subclassResponsibility ]

    rideDetails [
        Transcript show: '  Ride #', rideID printString,
                         ' [', self rideType, ']  ',
                         pickupLocation, ' -> ', dropoffLocation,
                         '  (', distance printString, ' mi)',
                         '  fare = $', (self fare roundTo: 0.01) printString;
                   nl
    ]
]

Ride subclass: #StandardRide
    instanceVariableNames: ''
    classVariableNames: ''
    package: 'RideSharing'.

StandardRide extend [
    fare     [ ^2.50 + (1.20 * distance) ]
    rideType [ ^'Standard' ]
]

Ride subclass: #PremiumRide
    instanceVariableNames: ''
    classVariableNames: ''
    package: 'RideSharing'.

PremiumRide extend [
    fare     [ ^5.00 + (2.50 * distance) + 3.00 ]
    rideType [ ^'Premium' ]
]

Object subclass: #Driver
    instanceVariableNames: 'driverID name rating assignedRides'
    classVariableNames: ''
    package: 'RideSharing'.

Driver extend [
    driverID: id name: n rating: r [
        driverID := id.
        name := n.
        rating := r.
        assignedRides := OrderedCollection new.
        ^self
    ]
    addRide: aRide [ assignedRides add: aRide ]

    getDriverInfo [
        Transcript show: 'Driver #', driverID printString, '  ', name,
                         '  rating=', rating printString,
                         '  rides=', assignedRides size printString;
                   nl.
        assignedRides do: [ :r | r rideDetails ]
    ]
]

Object subclass: #Rider
    instanceVariableNames: 'riderID name requestedRides'
    classVariableNames: ''
    package: 'RideSharing'.

Rider extend [
    riderID: id name: n [
        riderID := id.
        name := n.
        requestedRides := OrderedCollection new.
        ^self
    ]
    requestRide: aRide [ requestedRides add: aRide ]

    viewRides [
        Transcript show: 'Rider #', riderID printString, '  ', name,
                         '  history (', requestedRides size printString, ' rides):';
                   nl.
        requestedRides do: [ :r | r rideDetails ]
    ]
]

| r1 r2 r3 r4 allRides d1 rider1 |

r1 := StandardRide new rideID: 101 pickup: 'Airport' dropoff: 'Downtown'   distance: 8.0.
r2 := PremiumRide  new rideID: 102 pickup: 'Hotel'   dropoff: 'Conf Center' distance: 3.5.
r3 := StandardRide new rideID: 103 pickup: 'Mall'    dropoff: 'Suburbs'    distance: 12.2.
r4 := PremiumRide  new rideID: 104 pickup: 'Office'  dropoff: 'Restaurant' distance: 2.0.

allRides := OrderedCollection new.
allRides add: r1; add: r2; add: r3; add: r4.

Transcript show: '===== All rides (polymorphic dispatch) ====='; nl.
allRides do: [ :ride | ride rideDetails ].

Transcript nl; show: '===== Driver report ====='; nl.
d1 := Driver new driverID: 7 name: 'Alice' rating: 4.92.
d1 addRide: r1.
d1 addRide: r2.
d1 getDriverInfo.

Transcript nl; show: '===== Rider report ====='; nl.
rider1 := Rider new riderID: 900 name: 'Carlos'.
rider1 requestRide: r1.
rider1 requestRide: r3.
rider1 requestRide: r4.
rider1 viewRides.
