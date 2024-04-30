insert into depot_company_mapping (is_active, is_deleted, organization_id, company_id, depot_id)
values (1, 0, 1, 2, 7);

SELECT d.id, d.code, d.depot_name, d.depot_manager_id
FROM depot d INNER JOIN depot_company_mapping dm  ON d.id=dm.depot_id
WHERE d.depot_manager_id = 1
AND dm.company_id = 2
AND d.is_deleted = false
AND d.is_active = true;

SELECT d.id, d.code, d.depot_name,  l.name, l.location_type_id
FROM depot d INNER JOIN depot_location_map dm  ON d.id=dm.depot_id
INNER JOIN location l  ON l.id=dm.location_id
WHERE d.id = 7
AND d.is_deleted = false
AND d.is_active = true;


select i.company_id, i.transaction_date, it.inv_transaction_id,
i.transaction_type, it.quantity, it.product_id, it.batch_id,
from_store_id, to_store_id
from inv_transaction i
inner join inv_transaction_details it
on i.id = it.inv_transaction_id
where product_id=2;


select i.company_id, i.transaction_date, it.inv_transaction_id,
i.transaction_type, it.quantity, it.product_id, it.batch_id,
from_store_id, to_store_id, from_depot_id, to_depot_id
from inv_transaction i
inner join inv_transfer t
on t.inv_transaction_id = i.id
inner join inv_transaction_details it
on i.id = it.inv_transaction_id

alter table inv_transfer drop foreign key FKefltfks387eiqes8tfm9xfikw;
alter table inv_transfer drop column store_id;

select DISTINCT(it.id), it.company_id , it.transaction_type, it.transaction_date,
s.store_type, t.from_depot_id, t.to_depot_id, t.transfer_no,
t.driver_name, t.driver_contact_no, t.remarks,
d.depot_manager_id, d.depot_name, d.address, tod.depot_name to_depot_name, u.name user_name,
u.designation_id, ds.name designation_name,
SUM(itd.quantity) quantity
from inv_transaction it
inner join inv_transfer t on it.id=t.inv_transaction_id
and t.to_depot_id = 7
inner join inv_transaction_details itd on it.id=itd.inv_transaction_id
and (itd.qa_status IS NULL OR itd.qa_status = 'PASS')
inner join store s on s.id=itd.to_store_id
inner join depot d on d.id=t.from_depot_id
inner join depot tod on tod.id=t.to_depot_id
inner join application_user u on u.id=d.depot_manager_id
inner join designation ds on ds.id=u.designation_id
where it.is_active is true and it.is_deleted is false
and it.transaction_type in ('TRANSFER_SENT')
and s.store_type in ('IN_TRANSIT')
and it.company_id= 2

group by it.id, s.store_type, t.inv_transaction_id, t.from_depot_id, t.to_depot_id,t.transfer_no,
t.driver_name, t.driver_contact_no, t.remarks, tod.depot_name
order by it.transaction_date desc

select i.company_id, i.transaction_date, it.inv_transaction_id,
i.transaction_type, it.quantity, it.product_id, it.batch_id,
from_store_id, to_store_id, depot_id
from inv_transaction i
inner join inv_transaction_details it
on i.id = it.inv_transaction_id

ALTER TABLE payment_collection_adjustment
DROP FOREIGN KEY FK9s78cvfydr1aubd4r5wd86jj;

ALTER TABLE payment_collection_adjustment
DROP INDEX FK9s78cvfydr1aubd4r5wd86jj;

ALTER TABLE payment_collection_adjustment
CHANGE sales_invoice_id sales_invoice_id bigint(20) DEFAULT NULL;

alter table distributor_balance
drop column advance_payment;

alter table distributor_balance
drop column commission_balance;


TRUNCATE TABLE credit_debit_note;
TRUNCATE TABLE payment_collection_adjustment;
TRUNCATE TABLE payment_collection;

TRUNCATE TABLE sales_return_proposal_details;
TRUNCATE TABLE sales_return_adjustment;
TRUNCATE TABLE sales_return;
TRUNCATE TABLE sales_return_proposal;

TRUNCATE TABLE sales_invoice_challan_map;
TRUNCATE TABLE sales_invoice;

TRUNCATE TABLE batch_details;
TRUNCATE TABLE batch;
TRUNCATE TABLE inv_delivery_challan;
TRUNCATE TABLE inv_damage_details;
TRUNCATE TABLE inv_claim_details;
TRUNCATE TABLE inv_damage;
TRUNCATE TABLE inv_claim;
TRUNCATE TABLE inv_receive;
set foreign_key_checks =0;
TRUNCATE TABLE inv_transfer;
set foreign_key_checks =1;
TRUNCATE TABLE quality_inspection_details;
TRUNCATE TABLE quality_inspection;
TRUNCATE TABLE inv_transaction_details;
TRUNCATE TABLE inter_store_stock_movement;
TRUNCATE TABLE inv_transaction;

TRUNCATE TABLE picking_details;
TRUNCATE TABLE picking;

TRUNCATE TABLE sales_order_details;
TRUNCATE TABLE sales_order;

TRUNCATE TABLE material_receive_plan_history;
TRUNCATE TABLE material_receive_plan;

TRUNCATE TABLE sales_booking_details;
TRUNCATE TABLE sales_booking;

TRUNCATE TABLE sales_budget_details;
TRUNCATE TABLE sales_budget;

TRUNCATE TABLE collection_budget_details;
TRUNCATE TABLE collection_budget;

TRUNCATE TABLE credit_limit_proposal;

TRUNCATE TABLE distributor_balance
DELETE FROM document where id > 7

-------
--all setup data
TRUNCATE TABLE multi_layer_approval_process;
TRUNCATE TABLE multi_layer_approval_path;
TRUNCATE TABLE approval_step_feature_map;
TRUNCATE TABLE approval_step;

TRUNCATE TABLE credit_limit;

TRUNCATE TABLE product_trade_price;
TRUNCATE TABLE overriding_discount;
TRUNCATE TABLE trade_discount;
TRUNCATE TABLE semester;
TRUNCATE TABLE accounting_year;
TRUNCATE TABLE vat_setup;

location_manager_map
reporting_manager
proprietor;
distributor_guarantor;
distributor_sales_officer_map;
distributor;

payment_book;
payment;


--all inventory transactions
set foreign_key_checks =0;
TRUNCATE TABLE batch;
TRUNCATE TABLE batch_details;
TRUNCATE TABLE inv_delivery_challan;
TRUNCATE TABLE inv_damage_details;
TRUNCATE inv_claim_details;
TRUNCATE inv_damage;
TRUNCATE inv_claim;
TRUNCATE inv_receive;
TRUNCATE inv_transfer;
TRUNCATE quality_inspection_details;
TRUNCATE quality_inspection;
TRUNCATE inv_transaction_details;
TRUNCATE inter_store_stock_movement;
TRUNCATE inv_transaction;
set foreign_key_checks =1;


--start insert distributor opening balance after distributor-so mapping
INSERT INTO `distributor_balance` (`id`, `created_by`, `created_date`, `is_active`,
`is_deleted`, `last_modified_by`, `last_modified_date`, `balance`, `reference_no`,
`remaining_balance`, `transaction_date`, `organization_id`, `company_id`, `distributor_id`,
`invoice_nature_id`)
VALUES ('367', '2', '2023-08-22 15:50:34', 1, 0, 2, '2023-08-22 15:50:34',
4063.4, 'OB_JULY_23_CM048', 4063.4, '2023-08-22', 1, '2', '204', '2');

--insert paymentBook if not set payment book for distributor territory
INSERT INTO `payment_book`
(`id`, `created_by`, `created_date`, `is_active`, `is_deleted`, `last_modified_by`,
`last_modified_date`, `book_number`, `from_mr_no`, `issue_date`, `to_mr_no`,
`organization_id`, `company_id`, `location_id`)
VALUES ('25', '2', '2023-11-29 17:42:50', 1, 0, 2, '2023-11-29 17:42:50',
'M-00012', '1', '2023-11-29', 200, b'1', b'2', b'8');

INSERT INTO `payment_collection`
(`id`, `created_by`, `created_date`, `is_active`, `is_deleted`, `last_modified_by`,
`last_modified_date`, `action_taken_date`, `approval_status`, `collection_amount`,
`money_receipt_no`, `payment_date`, `payment_nature`, `payment_no`, `payment_type`,
`reference_no`, `remaining_amount`, `remarks`, `organization_id`, `action_taken_by_id`,
`collection_by_id`, `company_id`, `distributor_id`, `payment_book_id`, `approval_status_for_authorization`)
VALUES ('216', '118', '2023-11-29 17:43:08', b'1', b'0', b'2', b'2023-11-29 17:44:21',
b'2023-11-29 17:44:21', b'APPROVED', b'3500', b'1', b'2023-11-29', b'REGULAR', b'PC000216',
b'CASH', b'fdghd', b'3500', b'', b'1', b'2', b'118', b'2', b'205', b'25', b'APPROVED');
--end insert distributor opening balance after distributor-so mapping

--collection budget insert accounting year
INSERT INTO `sc_testserver_20231106`.`collection_budget`
(`id`, `created_by`, `created_date`, `is_active`, `is_deleted`, `last_modified_by`,
`last_modified_date`, `approval_status`, `budget_date`, `target_type`,
`organization_id`, `accounting_year_id`, `company_id`)
VALUES ('1', '2', '2023-10-05 11:09:50', 1, 0, 2, '2023-10-05 11:09:50',
'APPROVED', '2023-01-01', 'DISTRIBUTOR', '2', '2', '2');

--collection budget insert month-distributor-product
INSERT INTO `sc_testserver_20231106`.`collection_budget_details`
(`id`, `created_by`, `created_date`, `is_active`, `is_deleted`, `last_modified_by`,
`last_modified_date`, `collection_budget_amount`, `month`, `organization_id`,
`collection_budget_id`, `distributor_id`, `manufacturing_price`,
`product_trade_price`, `product_id`)
VALUES ('999', '2', '2023-10-05 11:10:12', 1, 0, 2, '2023-10-05 11:10:12',
3500, 11, '2', '1', '205', '0', '55', '67');

--sales budget insert accounting year
INSERT INTO `sc_testserver_20231106`.`sales_budget`
(`id`, `created_by`, `created_date`, `is_active`, `is_deleted`, `last_modified_by`,
`last_modified_date`, `approval_status`, `budget_date`, `target_type`, `organization_id`,
`accounting_year_id`, `company_id`)
VALUES ('1', '2', '2023-08-30 10:31:41', 1, 0, 2, '2023-08-30 10:31:41',
'APPROVED', '2023-01-01', 'DISTRIBUTOR', '2', '2', '2');
--sales budget insert  month-distributor-product
INSERT INTO `sc_testserver_20231106`.`sales_budget_details`
(`id`, `created_by`, `created_date`, `is_active`, `is_deleted`, `last_modified_by`,
`last_modified_date`, `manufacturing_price`, `month`, `product_trade_price`, `quantity`,
`organization_id`, `distributor_id`, `product_id`, `sales_budget_id`)
VALUES ('999', '2', '2023-08-30 10:31:48', 1, 0, 2, '2023-08-30 10:31:48',
'0', 11, '165', '1285', '2', '205', '118', '1');

SELECT * FROM snc_uat_20231224.location where name in ('Gazipur' ,
'Kishorgonj',
'Manikgonj' ,
'Netrokona' ,
'Narshinghdi' );

set foreign_key_checks =0;
TRUNCATE TABLE BATCH_JOB_EXECUTION;
TRUNCATE TABLE BATCH_JOB_EXECUTION_CONTEXT;
TRUNCATE TABLE BATCH_JOB_EXECUTION_PARAMS;
TRUNCATE TABLE BATCH_JOB_EXECUTION_SEQ;
TRUNCATE TABLE BATCH_JOB_INSTANCE;
TRUNCATE TABLE BATCH_JOB_SEQ;
TRUNCATE TABLE BATCH_STEP_EXECUTION_CONTEXT;
TRUNCATE TABLE BATCH_STEP_EXECUTION_SEQ;
TRUNCATE TABLE BATCH_STEP_EXECUTION;
set foreign_key_checks =1;

ALTER TABLE distributor AUTO_INCREMENT = 384;
ALTER TABLE distributor_guarantor AUTO_INCREMENT = 1167;
ALTER TABLE credit_limit AUTO_INCREMENT = 1167;
ALTER TABLE proprietor AUTO_INCREMENT = 1167;
ALTER TABLE distributor_sales_officer_map AUTO_INCREMENT = 1192;

--salesOfficerBooking count status
select lmm.application_user_id as salesOfficer, au.name salesOfficerName, count(sb.booking_no) bookingCount   from
            (select user_id, company_id from application_user_company_mapping where company_id in(2,3)) as aucm
                                         inner join
            (select rm.application_user_id, rmc.reporting_to_id from reporting_manager rm
            left join reporting_manager rmc on rm.application_user_id = rmc.reporting_to_id
            where rmc.application_user_id is null and rm.to_date is null and rm.is_active is true and rm.is_deleted is false) as lmm
            on aucm.user_id = lmm.application_user_id
            inner join application_user au on au.id = lmm.application_user_id
            and au.is_active is true and au.is_deleted is false
            left join sales_booking sb on sb.sales_officer_id= au.id
            and sb.company_id= aucm.company_id
            #where au.id=66
            group by lmm.application_user_id

update batch as t1,
(
select id, SUBSTRING_INDEX(batch_no, '-', 1) consign_no from batch ) as t2
set t1.consignment_no = t2.consign_no
where t1.id = t2.id and t1.id>=1


SELECT * FROM snc_uat_20231224.sales_booking

SELECT * FROM snc_uat_20231224.sales_booking where approval_status='APPROVED';

SELECT * FROM snc_uat_20231224.sales_booking where approval_status='REJECTED';

SELECT * FROM snc_uat_20231224.sales_booking where approval_status='AUTHORIZATION_FLOW';

SELECT * FROM snc_uat_20231224.sales_booking where approval_status='PENDING';

SELECT * FROM snc_uat_20231224.sales_booking where approval_status='DRAFT';

SELECT * FROM snc_uat_20231224.sales_booking where is_booking_stock_confirmed='Y';
SELECT count(sales_booking_id) FROM snc_uat_20231224.sales_booking sb inner join sales_booking_details sbd on sbd.sales_booking_id=sb.id
where sbd.sales_booking_status='TICKET_REQUESTED' group by sales_booking_id;

SELECT sales_booking_id, count(sales_booking_id) FROM snc_uat_20231224.sales_booking sb inner join sales_booking_details sbd on sbd.sales_booking_id=sb.id
 and sbd.sales_booking_status in('TICKET_REQUESTED', 'ORDER_CONVERTED')
 inner join sales_order_details sod on sod.sales_booking_details_id=sbd.id
group by sales_booking_id;

SELECT * FROM snc_uat_20231224.sales_order where approval_status = 'APPROVED';

SELECT count(sales_booking_id) FROM snc_uat_20231224.sales_booking sb inner join sales_booking_details sbd on sbd.sales_booking_id=sb.id
 where sbd.id not in (select sod.sales_booking_details_id from sales_order_details sod)
 #inner join sales_order_details sod on sod.sales_booking_details_id=sbd.id
group by sales_booking_id;

INSERT INTO distributor_company_map (distributor_id, location_id, company_id,
is_active, is_deleted, organization_id, created_by, created_date, last_modified_by,last_modified_date)
SELECT
    dsm.distributor_id,
    lo.id AS location_id,
    dsm.company_id, dsm.is_active, dsm.is_deleted, dsm.organization_id,
    dsm.created_by, dsm.created_date, dsm.last_modified_by,dsm.last_modified_date
FROM application_user au
INNER JOIN reporting_manager rm ON au.id = rm.application_user_id AND rm.to_date IS NULL
INNER JOIN location_manager_map lmm ON rm.reporting_to_id = lmm.application_user_id AND lmm.to_date IS NULL
INNER JOIN location lo ON lmm.location_id = lo.id
INNER JOIN distributor_sales_officer_map dsm ON dsm.sales_officer_id = au.id AND dsm.to_date IS NULL
GROUP BY dsm.company_id, au.id, lo.id, dsm.distributor_id, dsm.is_active, dsm.is_deleted, dsm.organization_id,
created_by, created_date, last_modified_by,last_modified_date
ORDER BY au.id;

UPDATE distributor d
INNER JOIN (
    SELECT
        d_inner.id AS distributor_id,
        lo.id AS new_location_id
    FROM distributor d_inner
    INNER JOIN distributor_sales_officer_map dsm ON d_inner.id = dsm.distributor_id
    INNER JOIN reporting_manager rm ON dsm.sales_officer_id = rm.application_user_id AND rm.to_date IS NULL
    INNER JOIN location_manager_map lmm ON rm.reporting_to_id = lmm.application_user_id AND lmm.to_date IS NULL
    INNER JOIN location lo ON lmm.location_id = lo.id
    -- WHERE conditions if needed, e.g., sb_inner.company_id = 2
    GROUP BY d_inner.id, lo.id
) AS update_info ON d.id = update_info.distributor_id
SET d.location_id = update_info.new_location_id;
