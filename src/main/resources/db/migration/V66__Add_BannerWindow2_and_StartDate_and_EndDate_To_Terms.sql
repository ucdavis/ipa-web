ALTER TABLE `Terms`
CHANGE COLUMN `BannerStartWindow` `BannerStartWindow1` DATE NULL DEFAULT NULL ,
CHANGE COLUMN `BannerEndWindow` `BannerEndWindow1` DATE NULL DEFAULT NULL ,
ADD COLUMN `BannerStartWindow2` DATE NULL DEFAULT NULL AFTER `BannerEndWindow1`,
ADD COLUMN `BannerEndWindow2` DATE NULL DEFAULT NULL AFTER `BannerStartWindow2`,
ADD COLUMN `StartDate` DATE NULL DEFAULT NULL AFTER `BannerEndWindow2`,
ADD COLUMN `EndDate` DATE NULL DEFAULT NULL AFTER `StartDate`;
