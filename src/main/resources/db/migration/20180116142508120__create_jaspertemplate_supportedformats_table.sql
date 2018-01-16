--
-- Name: jaspertemplate_supportedformats; Type: TABLE; Schema: reports; Owner: postgres
--

CREATE TABLE jaspertemplate_supportedformats (
    jaspertemplateid uuid NOT NULL,
    supportedformats character varying(255)
);

--
-- Name: jaspertemplate_supportedformats fkwnf16ufb6p8t2b6fasz39fgse; Type: FK CONSTRAINT; Schema: reports; Owner: postgres
--

ALTER TABLE ONLY jaspertemplate_supportedformats
    ADD CONSTRAINT fkwnf16ufb6p8t2b6fasz39fgse FOREIGN KEY (jaspertemplateid) REFERENCES jasper_templates(id);
