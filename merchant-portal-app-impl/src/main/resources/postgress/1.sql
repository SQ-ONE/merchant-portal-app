CREATE SCHEMA merchant_portal;

SET search_path TO merchant_portal;

CREATE TABLE merchant_risk_score (
  request_id          Int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  merchant_id   varchar(30) UNIQUE,
  old_risk   varchar(30),
  updated_risk  varchar(30),
  approval_flag  varchar(30),
  updated_timestamp timestamp
);

CREATE TABLE business_impact (
  merchant_id   varchar(30) UNIQUE,
  paymentType   varchar(30),
  paymentBlock  varchar(30),
  paymentInReview  varchar(30),
  paymentAllow varchar(30)
);
