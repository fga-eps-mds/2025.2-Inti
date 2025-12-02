// Post management functionality

// Use the existing apiService from config.js

// Create a new post
async function createPost(imageFile, description) {
  try {
    const formData = new FormData();
    formData.append("image", imageFile);
    formData.append("description", description);

    await apiService.createPost(formData);
    return { success: true };
  } catch (error) {
    console.error("Error creating post:", error);
    return { success: false, error: error.message };
  }
}

// Delete a post
async function deletePost(postId) {
  try {
    await apiService.deletePost(postId);
    return { success: true };
  } catch (error) {
    console.error("Error deleting post:", error);
    return { success: false, error: error.message };
  }
}

// Like a post
async function likePost(postId) {
  try {
    await apiService.likePost(postId);
    return { success: true };
  } catch (error) {
    console.error("Error liking post:", error);
    return { success: false, error: error.message };
  }
}

// Unlike a post
async function unlikePost(postId) {
  try {
    await apiService.unlikePost(postId);
    return { success: true };
  } catch (error) {
    console.error("Error unliking post:", error);
    return { success: false, error: error.message };
  }
}

// Get post details
async function getPostDetail(postId) {
  try {
    const post = await apiService.getPostDetail(postId);
    return { success: true, post };
  } catch (error) {
    console.error("Error getting post detail:", error);
    return { success: false, error: error.message };
  }
}

// Export functions if in Node.js environment
if (typeof module !== "undefined" && module.exports) {
  module.exports = {
    createPost,
    deletePost,
    likePost,
    unlikePost,
    getPostDetail,
  };
}
