(ns mailjure.models.recipient)

(defrecord Address [address1 address2 city region postal-code country])
(defrecord Recipient [_id email firstname lastname middlename ^Address address])
