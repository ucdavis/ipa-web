ALTER TABLE ReasonCategories ADD COLUMN archived BOOLEAN DEFAULT FALSE;

UPDATE ReasonCategories SET archived = TRUE;

INSERT INTO ReasonCategories (Id, Description)
VALUES
  (101, 'Internal Buyout'),
  (102, 'External Buyout'),
  (103, 'Course Release'),
  (104, 'Work Load Credit'),
  (105, 'Leave of Absence'),
  (106, 'Fellowship'),
  (107, 'Work Life Balance'),
  (108, 'Other');
