#include <iostream>
#include <iomanip>
#include <memory>
#include <string>
#include <vector>

class Ride {
protected:
    int    rideID;
    std::string pickupLocation;
    std::string dropoffLocation;
    double distance;
public:
    Ride(int id, std::string pickup, std::string dropoff, double miles)
        : rideID(id), pickupLocation(std::move(pickup)),
          dropoffLocation(std::move(dropoff)), distance(miles) {}

    virtual ~Ride() = default;

    int getID() const { return rideID; }

    virtual double fare() const = 0;
    virtual std::string rideType() const = 0;

    virtual void rideDetails() const {
        std::cout << "  Ride #" << rideID
                  << " [" << rideType() << "]  "
                  << pickupLocation << " -> " << dropoffLocation
                  << "  (" << distance << " mi)"
                  << "  fare = $" << std::fixed << std::setprecision(2) << fare()
                  << "\n";
    }
};

class StandardRide : public Ride {
public:
    StandardRide(int id, std::string pu, std::string drop, double miles)
        : Ride(id, std::move(pu), std::move(drop), miles) {}

    double fare() const override {
        return 2.50 + 1.20 * distance;
    }
    std::string rideType() const override { return "Standard"; }
};

class PremiumRide : public Ride {
public:
    PremiumRide(int id, std::string pu, std::string drop, double miles)
        : Ride(id, std::move(pu), std::move(drop), miles) {}

    double fare() const override {
        return 5.00 + 2.50 * distance + 3.00;
    }
    std::string rideType() const override { return "Premium"; }
};

class Driver {
    int    driverID;
    std::string name;
    double rating;
    std::vector<std::shared_ptr<Ride>> assignedRides;
public:
    Driver(int id, std::string n, double r)
        : driverID(id), name(std::move(n)), rating(r) {}

    void addRide(const std::shared_ptr<Ride>& ride) {
        assignedRides.push_back(ride);
    }

    void getDriverInfo() const {
        std::cout << "Driver #" << driverID << "  " << name
                  << "  rating=" << rating
                  << "  rides=" << assignedRides.size() << "\n";
        for (const auto& r : assignedRides) r->rideDetails();
    }
};

class Rider {
    int    riderID;
    std::string name;
    std::vector<std::shared_ptr<Ride>> requestedRides;
public:
    Rider(int id, std::string n)
        : riderID(id), name(std::move(n)) {}

    void requestRide(const std::shared_ptr<Ride>& ride) {
        requestedRides.push_back(ride);
    }

    void viewRides() const {
        std::cout << "Rider #" << riderID << "  " << name
                  << "  history (" << requestedRides.size() << " rides):\n";
        for (const auto& r : requestedRides) r->rideDetails();
    }
};

int main() {
    auto r1 = std::make_shared<StandardRide>(101, "Airport", "Downtown",    8.0);
    auto r2 = std::make_shared<PremiumRide> (102, "Hotel",   "Conf Center", 3.5);
    auto r3 = std::make_shared<StandardRide>(103, "Mall",    "Suburbs",    12.2);
    auto r4 = std::make_shared<PremiumRide> (104, "Office",  "Restaurant",  2.0);

    std::vector<std::shared_ptr<Ride>> allRides = { r1, r2, r3, r4 };

    std::cout << "===== All rides (polymorphic dispatch) =====\n";
    for (const auto& ride : allRides) {
        ride->rideDetails();
    }

    std::cout << "\n===== Driver report =====\n";
    Driver d1(7, "Alice",  4.92);
    d1.addRide(r1);
    d1.addRide(r2);
    d1.getDriverInfo();

    std::cout << "\n===== Rider report =====\n";
    Rider rider1(900, "Carlos");
    rider1.requestRide(r1);
    rider1.requestRide(r3);
    rider1.requestRide(r4);
    rider1.viewRides();

    return 0;
}
