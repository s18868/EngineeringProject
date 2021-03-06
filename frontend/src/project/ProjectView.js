import {Badge, Button, Col, Container, Form, Image, ListGroup, ListGroupItem, Modal, Row} from "react-bootstrap";
import {FaFacebookSquare, FaGithubSquare, FaKickstarter, FaYoutube} from "react-icons/all";
import "./ProjectView.css"
import default_project_picture from "../assets/images/default_project_picture.jpg"
import default_profile_photo from "../assets/images/default_profile_picture.jpg"
import React, {useEffect, useState} from "react";
import Rating from "./Rating";
import axios from "axios";
import Cookies from "js-cookie";
import {useParams} from "react-router-dom";
import ProjectComment from "./ProjectComment";
import {Alert} from "@mui/material";

function ProjectView(){

    const {id} = useParams();

    const [project, setProject] = useState();
    const [statusCode, setStatusCode] = useState();

    const [show, setShow] = useState(false);
    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);
    const [reasoning, setReasoning] = useState("");

    const [comments, setComments] = useState([]);
    const [responseMessage, setResponseMessage] = useState();
    const [responseCode, setResponseCode] = useState();
    const [showComment, setShowComment] = useState(false);
    const handleCloseComment = () => setShowComment(false);
    const handleShowComment = () => setShowComment(true);
    const [text, setText] = useState(null);
    
    useEffect(() => {
        axios.get(`http://localhost:8080/project/getProjectById/${id}`, {
        }).then(response => {
            setProject(response.data)
            setStatusCode(response.status)
        }).catch(err => {
            console.log(err.response)
            setStatusCode(err.response.status)
        })
        axios.get(`http://localhost:8080/comment/getProjectComments/${id}`, {
        }).then(response => {
            setComments(response.data)
        }).catch(err => {
            console.log(err.response)
        })
    }, [id])

    function join(){
        axios.post(`http://localhost:8080/project/join/${id}`,null,{headers:{
                'Authorization': Cookies.get("authorization")
            }}).then(response => {
            window.location.reload();
        }).catch(err => {
            console.log(err.response)
        })
    }

    function leave(){
        axios.post(`http://localhost:8080/project/leave/${id}`,null,{headers:{
                'Authorization': Cookies.get("authorization")
            }}).then(response => {
            window.location.reload();
        }).catch(err => {
            console.log(err.response)
        })
    }

    function report() {
        axios.post(`http://localhost:8080/report`,
            {"entityId" : id, "entityType" : "PROJECT", "reasoning" : `${reasoning}`},
            {headers:{'Authorization': Cookies.get("authorization")}
            })
            .then(response => {
                window.location.reload();
            })
            .catch(err =>  {
                window.alert(err.response.data.message)
            });
    }
    
    function commentSubmit(e) {
        e.preventDefault();
        axios.post(`http://localhost:8080/comment/createCommentProject/${id}`, {
            "text": text
        }, {headers:{
                'Authorization':Cookies.get("authorization")
            }})
            .then(response => {
                setResponseCode(response.status)
                setResponseMessage(response.data.message)
                axios.get(`http://localhost:8080/project/getProjectById/${id}`, {
                }).then(response => {
                    setProject(response.data)
                    handleCloseComment()
                }).catch(err => {
                    if (err.response.status === 404)  setResponseMessage("Project not found");
                    else setResponseMessage("Server error!");
                })
                axios.get(`http://localhost:8080/comment/getProjectComments/${id}`, {
                }).then(response => {
                    setComments(response.data)
                    handleCloseComment()
                }).catch(err => {
                    console.log(err.response)
                })
            }).catch(err => {
            console.log(err.response)
            setResponseCode(err.response.status)
        })
        
    }

    return(
        <Container className={"mt-5"}>
            <Row>
                <Col className={"col-2"}/>
                <Col className={"col-8"}>
                    { statusCode === 200 ?
                        <div>
                            <Row className={"mb-4"}>
                                <Col className={"PROJECT-VIEW-picture-section"}>
                                    { project.projectPhoto ?
                                        <div className={"PROJECT-VIEW-project-picture-col"}>
                                            <Image className={"PROJECT-VIEW-project-picture"}
                                                   src={`http://localhost:8080/photo?filename=${project.projectPhoto}`}/>
                                        </div>
                                        :
                                        <div className={"PROJECT-VIEW-project-picture-col"}>
                                            <Image className={"PROJECT-VIEW-project-picture"}
                                                   src={default_project_picture}/>
                                        </div>
                                    }
                                </Col>
                                <Col>
                                    <div className={"PROJECT-VIEW-base-info"}>
                                        <h1 className={"PROJECT-VIEW-title"}>
                                            {project.title}
                                        </h1>
                                        <hr className={"mb-3"}/>
                                        <h4 className={"PROJECT-VIEW-subtitle"}>
                                            {project.introduction}
                                        </h4>
                                    </div>
                                </Col>
                                <hr className={"mt-3 mb-1"}/>
                            </Row>
                            <Row>
                                <Col>
                                    <Row>
                                        <ListGroup>
                                            <ListGroup.Item>
                                                { project.averageRating === 0 ?
                                                    <h3>Project rating: <Badge pill bg="secondary">No votes</Badge></h3>
                                                    : project.averageRating > 0 && project.averageRating <= 2 ?
                                                        <h3>Project rating:
                                                            <Badge className={"ml-1"} pill bg="danger">{project.averageRating.toFixed(2)}</Badge>
                                                            <Badge className={"ml-2"} pill bg="primary">{project.numberOfVotes} votes</Badge>
                                                        </h3>
                                                        : project.averageRating > 2 && project.averageRating <= 3.5 ?
                                                            <h3>Project rating:
                                                                <Badge className={"ml-1"} pill bg="warning">{project.averageRating.toFixed(2)}</Badge>
                                                                <Badge className={"ml-2"} pill bg="primary">{project.numberOfVotes} votes</Badge>
                                                            </h3>
                                                            :
                                                            <h3>Project rating:
                                                                <Badge className={"ml-1"} pill bg="success">{project.averageRating.toFixed(2)}</Badge>
                                                                <Badge className={"ml-2"} pill bg="primary">{project.numberOfVotes} votes</Badge>
                                                            </h3>
                                                }
                                            </ListGroup.Item>
                                            <ListGroup.Item>
                                                <h3> Access:
                                                    { project.access === "PUBLIC" ?
                                                        <Badge className={"ml-2"} bg="success">{project.access}</Badge>
                                                        : project.access === "PROTECTED" ?
                                                            <Badge className={"ml-2"} bg="warning">{project.access}</Badge>
                                                            :
                                                            <Badge className={"ml-2"} bg="dark">{project.access}</Badge>
                                                    }
                                                </h3>
                                            </ListGroup.Item>
                                            <ListGroup.Item>
                                                <h3>
                                                    Creation date: <Badge bg="secondary">{project.creationDate.slice(0,10)}</Badge>
                                                </h3>
                                            </ListGroup.Item>
                                            <ListGroup.Item>
                                                {
                                                    project.categories.map((category, key) =>
                                                        <Badge key={key} className={"mr-1"} bg="primary">{category}</Badge>
                                                    )
                                                }
                                            </ListGroup.Item>
                                        </ListGroup>
                                    </Row>
                                </Col>
                                <Col>
                                    <Row className={"ml-3 mr-3 o"}>
                                        <center>
                                            <a className={"PROJECT-VIEW-author-holder"} href={`/profile/${project.authorId}`}>
                                                { project.authorPhoto ?
                                                    <Image className={"PROJECT-VIEW-author-picture"}
                                                           src={`http://localhost:8080/photo?filename=${project.authorPhoto}`}
                                                           roundedCircle={true}/>
                                                    :
                                                    <Image className={"PROJECT-VIEW-author-picture"}
                                                           src={default_profile_photo}
                                                           roundedCircle={true}/>
                                                }
                                                <h2 className={"ml-2"}>{project.authorUsername}</h2>
                                            </a>
                                        </center>
                                        { Cookies.get("authorization") ?
                                            <Row>
                                                {project.participants.includes(parseInt(Cookies.get("userId"))) ?
                                                    <>
                                                        { project.authorId !== parseInt(Cookies.get("userId")) ?
                                                            <>
                                                                <Button className={"mb-3 mt-4"} variant="success" size={"lg"} href={`/project/${project.projectId}/workspace`}>Workspace</Button>
                                                                <Button className={"mb-3"} variant="danger" onClick={() => leave()}>Leave</Button>
                                                            </>
                                                            :
                                                            <Button className={"mt-4 mb-3"} variant="success" href={`/project/${project.projectId}/workspace`}>Workspace</Button>
                                                        }
                                                    </>
                                                    :
                                                    <>
                                                        { project.access !== "PRIVATE" ?
                                                            <Button className={"mt-4"} variant="primary" onClick={() => join()}>Join</Button>
                                                            :
                                                            <></>
                                                        }
                                                    </>
                                                }
                                                <Button className={"mb-2 mt-2"} variant="danger" onClick={handleShow}>Report</Button>
                                                <Rating/>
                                            </Row>
                                            :
                                            <></>
                                        }
                                    </Row>
                                    <Modal show={show} onHide={handleClose}>
                                        <Modal.Header closeButton>
                                            <Modal.Title>Report {project.title}</Modal.Title>
                                        </Modal.Header>
                                        <Modal.Body>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Reason for the report</Form.Label>
                                                <Form.Control as="textarea" value={reasoning}
                                                              onChange={(e) => {setReasoning(e.target.value)}}
                                                              rows={3} required/>
                                            </Form.Group>
                                        </Modal.Body>
                                        <Modal.Footer>
                                            <Button variant="danger" onClick={report}>
                                                Send
                                            </Button>
                                        </Modal.Footer>
                                    </Modal>
                                </Col>
                            </Row>
                            <hr className={"mb-4 mt-2"}/>
                            <Row>
                                <h1 className={"mb-4"}>About project</h1>
                                <span className={"PROJECT-VIEW-description"}>
                                    {project.description}
                                </span>
                            </Row>
                            <hr className={"mb-4 mt-4"}/>
                            <Row>
                                <h1 className={"mb-3"}>Media</h1>
                                <div className={"PROJECT-VIEW-media"}>
                                    {project.youtubeLink ?
                                        <Col>
                                            <a href={`https://${project.youtubeLink}`}>
                                                <FaYoutube color={"black"} size={125}/>
                                            </a>
                                        </Col>
                                        :
                                        <></>
                                    }
                                    { project.githubLink ?
                                        <Col>
                                            <a href={`https://${project.githubLink}`}>
                                                <FaGithubSquare color={"black"} size={125}/>
                                            </a>
                                        </Col>
                                        :
                                        <></>
                                    }
                                    { project.facebookLink ?
                                        <Col>
                                            <a href={`https://${project.facebookLink}`}>
                                                <FaFacebookSquare color={"black"} size={125}/>
                                            </a>
                                        </Col>
                                        :
                                        <Col></Col>
                                    }
                                    { project.kickstarterLink ?
                                        <a href={`https://${project.kickstarterLink}`}>
                                            <Col>
                                                <FaKickstarter color={"black"} size={125}/>
                                            </Col>
                                        </a>
                                        :
                                        <Col></Col>
                                    }
                                </div>
                            </Row>
                            <Row>
                                <h1>Comments</h1>
                                {comments.length > 0 ?
                                    <ListGroup className={"list-group"}>
                                        {
                                            comments.map((comment, key) =>
                                                <ListGroupItem key={key}>
                                                    <ProjectComment comment={comment}/>
                                                </ListGroupItem>
                                            )
                                        }
                                    </ListGroup>
                                    :
                                    <h5 className={"mt-3"}>No comments found</h5>
                                }
                            </Row>
                            {Cookies.get("authorization") ?
                                <>
                                    <Button variant="primary" onClick={handleShowComment}>
                                        Add Comment
                                    </Button>
                                </>
                                :
                                <></>
                            }
                            <Modal
                                show={showComment}
                                onHide={handleCloseComment}
                                backdrop="static"
                            >
                                <Modal.Header closeButton>
                                    <Modal.Title></Modal.Title>
                                </Modal.Header>
                                <Modal.Body>
                                    <Form onSubmit={commentSubmit}>
                                        <Form.Group>
                                            <Form.Label>Your comment:</Form.Label>
                                            <Form.Control
                                                as="textarea"
                                                style={{ resize: 'none', height:'200px'}}
                                                onChange={(e)=>{setText(e.target.value)}}
                                                required>
                                            </Form.Control>
                                        </Form.Group>
                                        <Row>
                                            <Col className={"col-3"}/>
                                            <Button variant="secondary" onClick={handleCloseComment} className={"g"}>
                                                Close
                                            </Button>
                                            <Button type="submit" variant="primary" className={"g"}>
                                                Save Changes
                                            </Button>
                                        </Row>
                                    </Form>
                                    {responseMessage && responseCode === 200 ?
                                        <Alert variant={"success"}>
                                            <center>Nice comment!</center>
                                        </Alert>
                                        : responseMessage ?
                                            <Alert variant={"danger"}>
                                                <center>
                                                    {responseMessage}
                                                </center>
                                            </Alert>
                                            :
                                            <></>
                                    }
                                </Modal.Body>
                                <Modal.Footer>

                                </Modal.Footer>
                            </Modal>
                        </div>
                        :
                        <h2>Project not found</h2>
                    }
                </Col>
                <Col className={"col-2"}/>
            </Row>
        </Container>
    )
}
export default ProjectView