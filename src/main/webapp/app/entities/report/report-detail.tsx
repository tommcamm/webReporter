import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './report.reducer';

export const ReportDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const reportEntity = useAppSelector(state => state.report.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="reportDetailsHeading">
          <Translate contentKey="webReporterApp.report.detail.title">Report</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{reportEntity.id}</dd>
          <dt>
            <span id="title">
              <Translate contentKey="webReporterApp.report.title">Title</Translate>
            </span>
          </dt>
          <dd>{reportEntity.title}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="webReporterApp.report.description">Description</Translate>
            </span>
          </dt>
          <dd>{reportEntity.description}</dd>
          <dt>
            <span id="ipAddress">
              <Translate contentKey="webReporterApp.report.ipAddress">Ip Address</Translate>
            </span>
          </dt>
          <dd>{reportEntity.ipAddress}</dd>
          <dt>
            <Translate contentKey="webReporterApp.report.user">User</Translate>
          </dt>
          <dd>{reportEntity.user ? reportEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/report" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/report/${reportEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ReportDetail;
