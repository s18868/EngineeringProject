import "./Workspace.css"
import {Button, Col, Container, Form, ListGroup, Nav, Row, Tab} from "react-bootstrap";
import {useParams} from "react-router-dom";
import React, {useEffect, useRef, useState} from "react";
import axios from "axios";
import Cookies from "js-cookie";
import Member from "./Member";
import File from "./File";
import {FaFileUpload} from "react-icons/all";


function Workspace(){

    const {id} = useParams();

    //Files section
    const [fileTermSearch, setFileTermSearch] = useState("");
    const inputFile = useRef(null);
    const onButtonClick = () => {
        // `current` points to the mounted file input element
        inputFile.current.click();
    };
    function onUpload(){
        let file = document.getElementById("fileChooser").files[0]
        let bodyFormData = new FormData();
        bodyFormData.append("file", file);
        axios.post(`http://localhost:8080/file/upload?projectId=${id}`, bodyFormData,
            {headers:{'Authorization': Cookies.get("authorization")}}
        ).then(response => {
            window.location.reload();
        }).catch(err => {
            console.log(err.response)
        })
    }


    const [memberRole, setMemberRole] = useState(null)
    const [members, setMembers] = useState([]);
    const [files, setFiles] = useState([]);


    useEffect(() => {

        axios.get(`http://localhost:8080/project/getProjectMembers/${id}`,
            {headers: {'Authorization': Cookies.get("authorization")}
        }).then(response =>{
            setMembers(response.data)
            let userMember = response.data.filter(project => Number(project.userId) == Number(Cookies.get("userId")));
            if (userMember.length > 0) setMemberRole(userMember[0].projectRole)
            else setMemberRole(null)
        })
        .catch(err => {
            console.log(err.response)
            setMemberRole(null)
        })

        axios.get(`http://localhost:8080/project/getProjectFiles/${id}`,
            {headers: {'Authorization': Cookies.get("authorization")}
        }).then(response =>{
            setFiles(response.data)
        })
        .catch(err => {
            console.log(err.response)
        })

    },[])


    return(
        <Container className={"mt-5"}>
            <Row>
                {memberRole ?
                    <Tab.Container id="left-tabs-example" defaultActiveKey="first">
                        <Row>
                            <Col>
                                <Nav variant="pills" className="flex-column">
                                    <Nav.Item>
                                        <Nav.Link className={"mb-3"} eventKey="news">News</Nav.Link>
                                    </Nav.Item>
                                    <Nav.Item>
                                        <Nav.Link className={"mb-3"} eventKey="tasks">Tasks</Nav.Link>
                                    </Nav.Item>
                                    <Nav.Item>
                                        <Nav.Link className={"mb-3"} eventKey="files">Files</Nav.Link>
                                    </Nav.Item>
                                    <Nav.Item>
                                        <Nav.Link className={"mb-3"} eventKey="teams" disabled={true}>Teams</Nav.Link>
                                    </Nav.Item>
                                    <Nav.Item>
                                        <Nav.Link className={"mb-3"} eventKey="members">Members</Nav.Link>
                                    </Nav.Item>
                                    <Nav.Item>
                                        <Nav.Link className={"mb-3"} eventKey="panel">Project panel</Nav.Link>
                                    </Nav.Item>
                                </Nav>
                            </Col>
                            <Col sm={9}>
                                <Tab.Content>
                                    <Tab.Pane eventKey={"news"}>
                                        Hello
                                    </Tab.Pane>
                                    <Tab.Pane eventKey={"tasks"}>
                                        World
                                    </Tab.Pane>
                                    <Tab.Pane eventKey={"files"}>
                                        <Row>
                                            <Col>
                                                <center>
                                                    <Button onClick={() => {onButtonClick();}} >
                                                        <h4 className={"WORKSPACE-center-upload-button"}>
                                                            <Form.Control type="file" ref={inputFile} style={{display: 'none'}}
                                                                          id="fileChooser"
                                                                          onChange={(e) => {onUpload();}}/>
                                                            <FaFileUpload className={"mr-2"} size={35}/>
                                                            Upload file
                                                        </h4>
                                                    </Button>
                                                </center>
                                            </Col>
                                            <Col className={"WORKSPACE-center-upload-button"}>
                                                <center>
                                                    <Form>
                                                        <Form.Control type="text" placeholder="Search file"
                                                                      onChange={(e) => setFileTermSearch(e.target.value)}/>
                                                    </Form>
                                                </center>
                                            </Col>
                                        </Row>
                                        <hr/>
                                        { files.length > 0 ?
                                            <div className={"ml-5 WORKSPACE-file-section"}>
                                                { files.filter((f)=>{
                                                    if (fileTermSearch === ""){
                                                        return f
                                                    }
                                                    else if (f.fileName.toLowerCase().includes(fileTermSearch.toLowerCase())){
                                                        return f
                                                    }
                                                }).map((file, key) =>
                                                    <div key={key}>
                                                        <File file={file} role={memberRole}/>
                                                    </div>
                                                )}
                                            </div>
                                            :
                                            <center>
                                                <h1>Currently there are no files</h1>
                                            </center>
                                        }
                                    </Tab.Pane>
                                    <Tab.Pane eventKey={"members"}>
                                        <ListGroup>
                                            { members.map((member, key) =>
                                                <div key={key}>
                                                    <Member member={member}/>
                                                    <div/>
                                                </div>
                                            )}
                                        </ListGroup>
                                    </Tab.Pane>
                                </Tab.Content>
                            </Col>
                        </Row>
                    </Tab.Container>
                    :
                    <h1>You are not a member of this project</h1>
                }
            </Row>
        </Container>
    );

}
export default Workspace;