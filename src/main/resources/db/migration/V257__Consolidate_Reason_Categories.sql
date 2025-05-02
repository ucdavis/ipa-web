ALTER TABLE ReasonCategories ADD COLUMN archived BOOLEAN DEFAULT FALSE;

UPDATE ReasonCategories SET archived = TRUE;

INSERT INTO ReasonCategories (Id, Description)
VALUES
  (101, 'Internal Buyout'),
  (102, 'External Buyout'),
  (103, 'Course Release'),
  (104, 'Work Load Credit'),
  (105, 'Editor'),
  (106, 'Leave of Absence'),
  (107, 'Fellowship'),
  (108, 'FMLA'),
  (109, 'Work Life Balance'),
  (110, 'Conversion'),
  (111, 'Other');
