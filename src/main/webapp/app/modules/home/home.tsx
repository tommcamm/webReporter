import './home.scss';

import React from 'react';
import { Link } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Row, Col, Alert } from 'reactstrap';

import { useAppSelector } from 'app/config/store';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);

  return (
    <Row>
      <Col md="3">
        <h2>
          <Translate contentKey="home.title">Welcome to webReporter</Translate>
        </h2>
        <p className="lead">
          <Translate contentKey="home.subtitle">This will be the future homepage</Translate>
        </p>
        {account?.login ? (
          <div>
            <Alert color="success">
              <Translate contentKey="home.logged.message" interpolate={{ username: account.login }}>
                You are logged in as user {account.login}.
              </Translate>
            </Alert>
          </div>
        ) : (
          <div>
            <Alert color="warning">
              <Translate contentKey="global.messages.info.authenticated.prefix">If you want to </Translate>

              <Link to="/login" className="alert-link">
                <Translate contentKey="global.messages.info.authenticated.link"> sign in</Translate>
              </Link>
              <Translate contentKey="global.messages.info.authenticated.suffix">
                , you can try the default accounts:
                <br />- Administrator (login=&quot;admin&quot; and password=&quot;admin&quot;)
                <br />- User (login=&quot;user&quot; and password=&quot;user&quot;).
              </Translate>
            </Alert>

            <Alert color="warning">
              <Translate contentKey="global.messages.info.register.noaccount">You do not have an account yet?</Translate>&nbsp;
              <Link to="/account/register" className="alert-link">
                <Translate contentKey="global.messages.info.register.link">Register a new account</Translate>
              </Link>
            </Alert>
          </div>
        )}
        <p>This project is currently very WIP, a lot of functionalities are WIP.</p>
        <p>
          <Translate contentKey="home.like">If you like JHipster, do not forget to give us a star on</Translate>{' '}
          <a href="https://github.com/jhipster/generator-jhipster" target="_blank" rel="noopener noreferrer">
            GitHub
          </a>
          !
        </p>
      </Col>
    </Row>
  );
};

export default Home;
