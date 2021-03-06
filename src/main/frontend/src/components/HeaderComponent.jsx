import React, { Component } from 'react';
import UserService from '../services/UserService';
import AdminHeaderComponent from './admin/AdminHeaderComponent';
import NavBarComponent from './NavBarComponent';
import StudentHeaderComponent from './student/StudentHeaderComponent';

class HeaderComponent extends Component {

    constructor(props) {
        super(props);
        this.state = {
            user: null
        }
    }

    componentDidMount() {
        UserService.userGetSelf(
            null,
            (res) => this.setState({user: res})
        );
    }

    render() {
        if (this.state.user != null) {
            if (this.state.user.isAdmin)
                return <AdminHeaderComponent user={this.state.user}/>
            else if (this.state.user.promotionId !== "")
                return <StudentHeaderComponent user={this.state.user}/>
            else
                return null;
        } else
            return <div>loading...</div>
    }
}

export default HeaderComponent;