CREATE INDEX local_page_idx_page_id ON universal_page(univ_id);
CREATE INDEX local_page_idx_page_title ON universal_page(univ_id, page_type);